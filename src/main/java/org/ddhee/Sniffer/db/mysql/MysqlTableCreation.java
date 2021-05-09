package org.ddhee.Sniffer.db.mysql;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.ddhee.Sniffer.db.Argon2Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlTableCreation {
  // Reset database
  public static void main(String[] args) {
    Connection conn = null;

    try {
      System.out.println("Connecting to " + MysqlDBUtil.DB_URL);
//      Class.forName(MysqlDBUtil.DRIVER).newInstance();
      conn = DriverManager.getConnection(MysqlDBUtil.DB_URL);
    } catch (SQLException e) {
      System.out.println("SQLException " + e.getMessage());
      System.out.println("SQLState " + e.getSQLState());
      System.out.println("VendorError " + e.getErrorCode());
    }

    if (conn == null) {
      return;
    }

    System.out.println("Connection established. ");

    try {
      Statement stmt = conn.createStatement();

      // Drop old tables if any
      String sql = "DROP TABLE IF EXISTS history";
      stmt.executeUpdate(sql);
      sql = "DROP TABLE IF EXISTS users";
      stmt.executeUpdate(sql);
      sql = "DROP TABLE IF EXISTS restaurants";
      stmt.executeUpdate(sql);

      // Create new tables
      sql = "CREATE TABLE restaurants "
              + "(business_id VARCHAR(255) NOT NULL, "
              + "name VARCHAR(255), categories VARCHAR(255), "
              + "city VARCHAR(255), state VARCHAR(255), "
              + "stars FLOAT, address VARCHAR(255), "
              + "latitude FLOAT, longitude FLOAT, "
              + "url VARCHAR(255), image_url VARCHAR(255), "
              + " PRIMARY KEY (business_id))";
      stmt.executeUpdate(sql);

      sql = "CREATE TABLE users "
              + "(user_id VARCHAR(255) NOT NULL, "
              + "password VARCHAR(255) NOT NULL, "
              + "first_name VARCHAR(255), last_name VARCHAR(255), "
              + " PRIMARY KEY (user_id))";
      stmt.executeUpdate(sql);


      sql = "CREATE TABLE history "
              + "(visit_history_id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT, "
              + "user_id VARCHAR(255) NOT NULL, "
              + "business_id VARCHAR(255) NOT NULL, "
              + "last_visit_time TIMESTAMP NOT NULL DEFAULT NOW(), "
              + " PRIMARY KEY (visit_history_id), "
              + " FOREIGN KEY (business_id) REFERENCES restaurants(business_id), "
              + " FOREIGN KEY (user_id) REFERENCES users(user_id))";
      stmt.executeUpdate(sql);

      System.out.println("Table reset successfully. ");

      // Test inserting data. Insert a fake user
      String userId = "ddhee";
      String password ="123";
      String firstName = "Dd";
      String lastName = "Hee";
      Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
      String hashedPwd = argon2.hash(Argon2Config.iterations,
              Argon2Config.memory, Argon2Config.parallelism, password.toCharArray());
      sql = "INSERT  INTO users VALUES ('" + userId + "', '" + hashedPwd
              + "', '" + firstName + "', '" + lastName + "')";
      int result = stmt.executeUpdate(sql);
      System.out.println("Inserted fake user ddhee");
    } catch (SQLException e) {
      System.out.println("SQLException " + e.getMessage());
      System.out.println("SQLState " + e.getSQLState());
      System.out.println("VendorError " + e.getErrorCode());
    }
  }
}
