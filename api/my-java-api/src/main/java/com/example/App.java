package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class App {

  public static void main(String[] args) throws IOException {
    var config = new Properties();
    try (var reader = Files.newBufferedReader(Path.of(".env"))) {
      config.load(reader);
    }

    var connUrl = config.getProperty("CONN_URL");
    if (connUrl == null || connUrl.isBlank()) {
      throw new IllegalStateException("CONN_URL is missing from .env");
    }

    try (var conn = DriverManager.getConnection(connUrl);
         var stmt = conn.createStatement();
         var rs = stmt.executeQuery("select extname, extversion from pg_extension")) {
      while (rs.next()) {
        var extname = rs.getString("extname");
        var extversion = rs.getString("extversion");
        System.out.println(extname + ", " + extversion);
      }
    } catch (SQLException ex) {
      System.err.println(ex.getMessage());
    }
  }
}
