package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class App {

  public static void main(String[] args) throws IOException, SQLException {
    var config = new Properties();
    System.out.println("Working dir: " + Path.of("").toAbsolutePath());

    try (var reader = Files.newBufferedReader(Path.of("api/my-java-api/.env"))) {
      config.load(reader);
    }

    var connUrl = config.getProperty("CONN_URL");
    if (connUrl == null || connUrl.isBlank()) {
      throw new IllegalStateException("CONN_URL is missing from .env");
    }

    //sql_testing.seed(connUrl);
    new RosaryAPI(connUrl).start(8081);
  }
}