package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RosaryAPI {

  private final String connUrl;

  public RosaryAPI(String connUrl) {
    this.connUrl = connUrl;
  }

  public void start(int port) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/rosary", this::handleRosary);
    server.start();
    System.out.println("API listening on http://localhost:" + port);
  }

  private void handleRosary(HttpExchange ex) throws IOException {
    if (!requireGet(ex)) {
      return;
    }

    Map<String, String> params = parseQuery(ex.getRequestURI().getRawQuery());

    String mystery = params.get("mystery");
    Integer decade;
    Integer num;
    try {
        decade = params.containsKey("decade") ? Integer.valueOf(params.get("decade")) : null;
        num = params.containsKey("number") ? Integer.valueOf(params.get("number")) : null;
    } catch (NumberFormatException e) {
        sendJson(ex, 400, "{\"error\":\"decade and number must be integers\"}");
        return;
    }

    StringBuilder sql = new StringBuilder("""
        SELECT r.mystery, r.decade, r.number, r.reading, p.name, p.text, i.image
        FROM rosary r
        INNER JOIN prayers p ON p.prayer_id = r.prayer_id
        LEFT JOIN images i ON i.mystery = r.mystery AND i.decade = r.decade
        WHERE 1 = 1
        """);

    List<Object> values = new ArrayList<>();
    if (mystery != null && !mystery.isBlank()) {
        sql.append("AND r.mystery = ? ");
        values.add(mystery);
    }
    if (decade != null) {
        sql.append("AND r.decade = ? ");
        values.add(decade);
    }
    if (num != null) {
        sql.append("AND r.number = ? ");
        values.add(num);
    }

    try (Connection conn = DriverManager.getConnection(connUrl);
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        for (int i = 0; i < values.size(); i++) {
            Object v = values.get(i);
            if (v instanceof Integer n) {
                ps.setInt(i + 1, n);
            } else {
                ps.setString(i + 1, (String) v);
            }
        }

        try (ResultSet rs = ps.executeQuery()) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(',');
                }
                first = false;
                byte[] img = rs.getBytes("image");
                json.append("{\"mystery\":").append(jsonString(rs.getString("mystery")))
                    .append(",\"decade\":").append(rs.getInt("decade"))
                    .append(",\"number\":").append(rs.getInt("number"))
                    .append(",\"reading\":").append(jsonString(rs.getString("reading")))
                    .append(",\"prayerName\":").append(jsonString(rs.getString("name")))
                    .append(",\"prayerText\":").append(jsonString(rs.getString("text")))
                    .append(",\"image\":").append(img == null ? "null" : jsonString(Base64.getEncoder().encodeToString(img)))
                    .append('}');
            }
            json.append(']');
            sendJson(ex, 200, json.toString());
        }
    } catch (SQLException e) {
        sendError(ex, e);
    }
  }

  // ---------- helpers ----------

  private boolean requireGet(HttpExchange ex) throws IOException {
    if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
      return true;
    }
    sendJson(ex, 405, "{\"error\":\"Method not allowed. Use GET.\"}");
    return false;
  }

  private void sendError(HttpExchange ex, SQLException e) throws IOException {
    System.err.println("Database error: " + e.getMessage());
    sendJson(ex, 500, "{\"error\":" + jsonString("Database error: " + e.getMessage()) + "}");
  }

  private static void sendJson(HttpExchange ex, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    ex.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    }
  }

  private static Map<String, String> parseQuery(String rawQuery) {
    Map<String, String> params = new HashMap<>();
    if (rawQuery == null || rawQuery.isBlank()) {
      return params;
    }
    for (String pair : rawQuery.split("&")) {
      String[] kv = pair.split("=", 2);
      String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
      String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
      params.put(key, value);
    }
    return params;
  }

  /** Quotes and escapes a value for JSON, or returns null for SQL NULL. */
  private static String jsonString(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder("\"");
    for (char c : value.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }
}
