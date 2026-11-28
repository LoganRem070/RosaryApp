package com.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.LinkedHashSet;
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

  private record Step(String mystery, int decade, int number,
                      String reading, String prayerName, String prayerText) {}

  private static final class Room {
    final String id;
    final List<String> mysteries;
    final String language;
    final List<Step> steps;
    final Set<WebSocket> members = ConcurrentHashMap.newKeySet();
    int position = -1; // -1 = lobby, 0..n-1 = current bead, n = complete

    Room(String id, List<String> mysteries, String language, List<Step> steps) {
      this.id = id;
      this.mysteries = mysteries;
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
    if (origin != null && !origin.isEmpty() && !RosaryAPI.isAllowedOrigin(origin)) {
      throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, "Origin not allowed");
    }
    return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    // Nothing until the client sends create or join
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
        case "create" -> createRoom(conn, msg);
        case "join" -> joinRoom(conn, msg.get("roomId").getAsString().trim().toUpperCase());
        case "start" -> startRoom(conn);
        case "next" -> nextStep(conn);
        case "prev" -> prevStep(conn);
        case "leave" -> leave(conn);
        default -> sendError(conn, "Unknown message type: " + type);
      }
    } catch (Exception e) {
      System.err.println("Room request failed: " + e);
      sendError(conn, "Request failed: " + e.getMessage());
    }
  }

  // ---------- room actions ----------

  private void createRoom(WebSocket conn, JsonObject msg) throws Exception {
    List<String> mysteries = readMysteries(msg);
    if (mysteries.isEmpty()) {
      sendError(conn, "Choose at least one mystery");
      return;
    }
    for (String m : mysteries) {
      if (!MYSTERIES.contains(m)) {
        sendError(conn, "Unknown mystery: " + m);
        return;
      }
    }

    String language = msg.has("language") ? msg.get("language").getAsString().trim() : "";
    if (language.isEmpty()) {
      language = "English";
    }

    // Steps for each mystery, in the order the mysteries were chosen
    List<Step> steps = new ArrayList<>();
    for (String m : mysteries) {
      steps.addAll(loadSteps(m));
    }
    if (steps.isEmpty()) {
      sendError(conn, "No prayers found for the selected mysteries");
      return;
    }

    leave(conn);
    Room room = new Room(newRoomId(), mysteries, language, steps);
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

  private void startRoom(WebSocket conn) throws Exception {
    Room room = memberships.get(conn);
    if (room == null) {
      sendError(conn, "Join or create a room first");
      return;
    }
    synchronized (room) {
      int first = Math.max(room.position, 0);
      publish(room, first);
    }
  }

  private void nextStep(WebSocket conn) throws Exception {
    Room room = memberships.get(conn);
    if (room == null) {
      sendError(conn, "Join or create a room first");
      return;
    }
    synchronized (room) {
      if (room.position < 0) {
        sendError(conn, "Press Start first");
        return;
      }
      publish(room, Math.min(room.position + 1, room.steps.size()));
    }
  }

  private void prevStep(WebSocket conn) throws Exception {
    Room room = memberships.get(conn);
    if (room == null) {
      sendError(conn, "Join or create a room first");
      return;
    }
    synchronized (room) {
      if (room.position <= 0) {
        return; // already at the first bead, or still in the lobby
      }
      publish(room, room.position - 1);
    }
  }

  private void addMember(WebSocket conn, Room room) throws Exception {
    synchronized (room) {
      room.members.add(conn);
      memberships.put(conn, room);
      publish(room, room.position); // everyone sees the new participant count
    }
  }

  private void leave(WebSocket conn) {
    Room room = memberships.remove(conn);
    if (room == null) {
      return;
    }
    synchronized (room) {
      room.members.remove(conn);
      if (room.members.isEmpty()) {
        rooms.remove(room.id);
        return;
      }
      try {
        publish(room, room.position);
      } catch (Exception e) {
        System.err.println("Could not update room " + room.id + ": " + e.getMessage());
      }
    }
  }

  /**
   * Renders the state first, so a translation failure leaves the room unchanged.
   * Then sets the position and sends the same message to every member.
   */
  private void publish(Room room, int index) throws IOException, InterruptedException {
    String json = render(room, index);
    room.position = index;
    for (WebSocket member : room.members) {
      if (member.isOpen()) {
        member.send(json);
      }
    }
  }

  // ---------- rendering ----------

  private String render(Room room, int index) throws IOException, InterruptedException {
    if (index < 0) {
      return base("lobby", room, index).toString();
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
    o.addProperty("mystery", s.mystery());
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
    JsonArray mysteries = new JsonArray();
    for (String m : room.mysteries) {
      mysteries.add(m);
    }
    o.add("mysteries", mysteries);
    o.addProperty("language", room.language);
    o.addProperty("participants", room.members.size());
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
    // Only rows that have a reading are beads. Adjust this filter once the data is final.
    String sql = """
        SELECT r.mystery, r.decade, r.number, r.reading, p.name, p.text
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
              rs.getString("mystery"),
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

  private static List<String> readMysteries(JsonObject msg) {
    Set<String> out = new LinkedHashSet<>();
    if (msg.has("mysteries") && msg.get("mysteries").isJsonArray()) {
      for (JsonElement e : msg.getAsJsonArray("mysteries")) {
        out.add(e.getAsString());
      }
    } else if (msg.has("mystery")) {
      out.add(msg.get("mystery").getAsString());
    }
    return new ArrayList<>(out);
  }

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
}