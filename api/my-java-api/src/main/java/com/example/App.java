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

  public static void main(String[] args) throws IOException, SQLException, InterruptedException {
    var config = new Properties();
    Path workingDirectory = Path.of("").toAbsolutePath();
    Path envPath = workingDirectory.resolve(".env");
    if (!Files.isRegularFile(envPath)) {
      envPath = workingDirectory.resolve("api/my-java-api/.env");
    }
    if (!Files.isRegularFile(envPath)) {
      throw new IOException("Could not find .env in " + workingDirectory
          + " or its api/my-java-api subdirectory");
    }

    try (var reader = Files.newBufferedReader(envPath)) {
      config.load(reader);
    }

    var connUrl = config.getProperty("CONN_URL");
    if (connUrl == null || connUrl.isBlank()) {
      throw new IllegalStateException("CONN_URL is missing from .env");
    }

    //sql_testing.seed(connUrl);
    //new RosaryAPI(connUrl).start(8081);
    Gemini_Translation gemini = new Gemini_Translation(config.getProperty("GEM_API_KEY"));
    new RosaryAPI(connUrl, gemini).start(8081);
  }
}