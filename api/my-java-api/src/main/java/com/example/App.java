package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

public class App {

  public static void main(String[] args) throws IOException {
    var config = new Properties();
    System.out.println("Working dir: " + Path.of("").toAbsolutePath());
    try (var reader = Files.newBufferedReader(Path.of("api/my-java-api/.env"))) {
      config.load(reader);
    }

    var connUrl = config.getProperty("CONN_URL");
    if (connUrl == null || connUrl.isBlank()) {
      throw new IllegalStateException("CONN_URL is missing from .env");
    }

    try {
      sql_testing.seed(connUrl);
    } catch (SQLException ex) {
      System.err.println(ex.getMessage());
    }
  }
}
