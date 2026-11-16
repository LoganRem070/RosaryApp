package com.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

public final class RosaryRoomServer extends WebSocketServer {

  @FunctionalInterface
  public interface Translator {
    String translate(String text, String language) throws IOException, InterruptedException;
  }

  private static final Set<String> MYSTERIES = Set.of("Joyful", "Luminous", "Sorrowful", "Glorious");
  private static final String ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final SecureRandom RANDOM = new SecureRandom();

  private record Step(int decade, int number, String reading, String prayerName, String prayerText) {}

  private static final class Room {
    final String id;
    final String mystery;
    final String language;
    final List<Step> steps;
    final Set<WebSocket> members = ConcurrentHashMap.newKeySet();
    int position = -1; // -1 = not started, steps.size() = finished

    Room(String id, String mystery, String language, List<Step> steps) {
      this.id = id;
      this.mystery = mystery;
      this.language = language;
      this.steps = steps;
    }
  }

  private final String connUrl;
  private final Translator translator;
  private final Map<String, Room> rooms = new ConcurrentHashMap<>();
  private final Map<WebSocket, Room> memberships = new ConcurrentHashMap<>();

  public RosaryRoomServer(InetSocketAddress address, String connUrl, Translator translator) {
    super(address);
    this.connUrl = connUrl;
    this.translator = translator;
  }

  @Override
  public void onStart() {
    System.out.println("Room server listening on ws://localhost:" + getPort());
  }

  @Override
  public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(
      WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
    String origin = request.getFieldValue("Origin");
    // Browsers send Origin; tools like curl do not, so a missing Origin is allowed
    if (origin != null && !origin.isEmpty() && !RosaryAPI.isAllowedOrigin(origin)) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Origin not allowed");
    }
    return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    // Nothing to do until the client sends create or join
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    leave(conn);
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    System.err.println("WebSocket error: " + ex.getMessage());
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    try {
      JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
      String type = msg.get("type").getAsString();
      switch (type) {
        case "create" -> createRoom(conn, msg.get("mystery").getAsString(),
            optional(msg, "language", "English"));
        case "join" -> joinRoom(conn, msg.get("roomId").getAsString().toUpperCase());
        case "next" -> nextStep(conn);
        case "prev" -> prevStep(conn);
        case "leave" -> leave(conn);
        default -> sendError(conn, "Unknown message type: " + type);
      }
    } catch (Exception e) {
      System.err.println("Room request failed: " + e);
      sendError(conn, "Bad request or server error: " + e.getMessage());
    }
  }

  // ---------- room actions ----------

  private void createRoom(WebSocket conn, String mystery, String language) throws Exception {
    if (!MYSTERIES.contains(mystery)) {
      sendError(conn, "Unknown mystery: " + mystery);
      return;
    }
    String lang = language.isBlank() ? "English" : language.trim();

    List<Step> steps = loadSteps(mystery);
    if (steps.isEmpty()) {
      sendError(conn, "No prayers found for " + mystery);
      return;
    }

    leave(conn);
    Room room = new Room(newRoomId(), mystery, lang, steps);
    rooms.put(room.id, room);
    addMember(conn, room);
  }

  private void joinRoom(WebSocket conn, String roomId) throws Exception {
    Room room = rooms.get(roomId);
    if (room == null) {
      sendError(conn, "No room with code " + roomId);
      return;
    }
    leave(conn);
    addMember(conn, room);
  }

  private void nextStep(WebSocket conn) throws Exception {
    Room room = memberships.get(conn);
    if (room == null) {
      sendError(conn, "Join or create a room first");
      return;
    }

    // The lock keeps messages in order when two members press Next at the same time
    synchronized (room) {
      int candidate = Math.min(room.position + 1, room.steps.size());
      String json = render(room, candidate);
      room.position = candidate;
      for (WebSocket member : room.members) {
        if (member.isOpen()) {
          member.send(json);
        }
      }
    }
  }

  private void prevStep(WebSocket conn) throws Exception {
    Room room = memberships.get(conn);
    if (room == null) {
      sendError(conn, "Join or create a room first");
      return;
    }

    // The lock keeps messages in order when two members press Next at the same time
    synchronized (room) {
      int candidate = Math.max(room.position - 1, -1);
      String json = render(room, candidate);
      room.position = candidate;
      for (WebSocket member : room.members) {
        if (member.isOpen()) {
          member.send(json);
        }
      }
    }
  }

  private void addMember(WebSocket conn, Room room) throws Exception {
    synchronized (room) {
      room.members.add(conn);
      memberships.put(conn, room);
      conn.send(render(room, room.position));
    }
  }

  private void leave(WebSocket conn) {
    Room room = memberships.remove(conn);
    if (room == null) {
      return;
    }
    room.members.remove(conn);
    if (room.members.isEmpty()) {
      rooms.remove(room.id);
    }
  }

  // ---------- rendering ----------

  /** Builds the JSON message for a position in the room. Translates only if needed. */
  private String render(Room room, int index) throws IOException, InterruptedException {
    if (index < 0) {
      return base("waiting", room, index).toString();
    }
    if (index >= room.steps.size()) {
      return base("complete", room, room.steps.size()).toString();
    }

    Step s = room.steps.get(index);
    String reading = s.reading();
    String prayerName = s.prayerName();
    String prayerText = s.prayerText();

    if (!room.language.equalsIgnoreCase("English")) {
      reading = translator.translate(reading, room.language);
      prayerName = translator.translate(prayerName, room.language);
      prayerText = translator.translate(prayerText, room.language);
    }

    JsonObject o = base("step", room, index);
    o.addProperty("decade", s.decade());
    o.addProperty("number", s.number());
    o.addProperty("reading", reading);
    o.addProperty("prayerName", prayerName);
    o.addProperty("prayerText", prayerText);
    return o.toString();
  }

  private JsonObject base(String type, Room room, int index) {
    JsonObject o = new JsonObject();
    o.addProperty("type", type);
    o.addProperty("roomId", room.id);
    o.addProperty("mystery", room.mystery);
    o.addProperty("language", room.language);
    o.addProperty("index", index);
    o.addProperty("total", room.steps.size());
    return o;
  }

  private void sendError(WebSocket conn, String message) {
    JsonObject o = new JsonObject();
    o.addProperty("type", "error");
    o.addProperty("message", message);
    conn.send(o.toString());
  }

  // ---------- database ----------

  private List<Step> loadSteps(String mystery) throws SQLException {
    String sql = """
        SELECT r.decade, r.number, r.reading, p.name, p.text
        FROM rosary r
        INNER JOIN prayers p ON p.prayer_id = r.prayer_id
        WHERE r.mystery = ?
        ORDER BY r.decade, r.number
        """;

    List<Step> steps = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(connUrl);
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, mystery);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          steps.add(new Step(
              rs.getInt("decade"),
              rs.getInt("number"),
              rs.getString("reading"),
              rs.getString("name"),
              rs.getString("text")));
        }
      }
    }
    return steps;
  }

  // ---------- helpers ----------

  private String newRoomId() {
    while (true) {
      StringBuilder sb = new StringBuilder(6);
      for (int i = 0; i < 6; i++) {
        sb.append(ID_CHARS.charAt(RANDOM.nextInt(ID_CHARS.length())));
      }
      String id = sb.toString();
      if (!rooms.containsKey(id)) {
        return id;
      }
    }
  }

  private static String optional(JsonObject o, String key, String fallback) {
    return o.has(key) ? o.get(key).getAsString() : fallback;
  }
}
