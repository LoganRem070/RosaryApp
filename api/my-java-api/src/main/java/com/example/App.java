package com.example;

import java.sql.DriverManager;
import java.sql.SQLException;


public class App {

  static final String CONN_URL = "jdbc:postgresql://wlw7dia5b0.lq4yhtpxri.tsdb.cloud.timescale.com:30951/tsdb?sslmode=require&user=tsdbadmin&password=j61jo9eey58gdvi9";

  public static void main(String[] args) {
    try (var conn = DriverManager.getConnection(CONN_URL)) {

      var stmt = conn.createStatement();
      var rs = stmt.executeQuery("select extname, extversion from pg_extension");

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
