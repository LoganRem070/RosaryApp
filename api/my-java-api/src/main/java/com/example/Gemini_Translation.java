package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class Gemini_Translation {

  private static final String MODEL = "gemini-3.5-flash-lite";
  private static final String ENDPOINT =
      "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

  private final String apiKey;
  private final HttpClient http;

  public Gemini_Translation(String apiKey) {
    this.apiKey = apiKey;
    this.http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  /** Sends a prompt to Gemini and returns the text of its reply. */
  public String ask(String prompt) throws IOException, InterruptedException {
    String body = """
        {"contents":[{"parts":[{"text":%s}]}]}
        """.formatted(jsonString(prompt));

    HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
        .timeout(Duration.ofSeconds(60))
        .header("Content-Type", "application/json")
        .header("x-goog-api-key", apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
        .build();

    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IOException("Gemini returned " + response.statusCode() + ": " + response.body());
    }
    return extractText(response.body());
  }

  /**
   * Pulls the reply text out of the response JSON. The text is the value of the first
   * "text" field, so this is a simple search, not a full JSON parser.
   */
  static String extractText(String json) throws IOException {
    String key = "\"text\": \"";
    int start = json.indexOf(key);
    if (start < 0) {
      // Some responses omit the space after the colon
      key = "\"text\":\"";
      start = json.indexOf(key);
    }
    if (start < 0) {
      throw new IOException("No text in Gemini response: " + json);
    }
    start += key.length();

    StringBuilder out = new StringBuilder();
    for (int i = start; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '\\' && i + 1 < json.length()) {
        char n = json.charAt(++i);
        switch (n) {
          case 'n' -> out.append('\n');
          case 't' -> out.append('\t');
          case 'r' -> out.append('\r');
          case 'u' -> {
            out.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
            i += 4;
          }
          default -> out.append(n);   // covers \" and \\
        }
      } else if (c == '"') {
        break;
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }

  /** Quotes a string as a JSON value, escaping characters that would break the request. */
  private static String jsonString(String value) {
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