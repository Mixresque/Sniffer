package org.ddhee.Sniffer.db.mysql;

import org.ddhee.Sniffer.db.Argon2Config;
import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.entity.Restaurant;
import org.ddhee.Sniffer.external.YelpAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MysqlDBConnection implements DBConnection {
  private Connection conn = null;
  private boolean connected;

  public MysqlDBConnection(String url) {
    try {
      Class.forName(MysqlDBUtil.DRIVER).newInstance();
      conn = DriverManager.getConnection(url);
      if (conn != null) {
        connected = true;
      }
    } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      connected = false;
    }
  }

  public MysqlDBConnection() {
    this(MysqlDBUtil.DB_URL);
  }

  @Override
  public void close() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      connected = false;
    }
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public Boolean verifyLogin(String userId, String password) {
    if (!connected || conn == null || userId == null || password == null) {
      return false;
    }

    try {
      String sql = "SELECT password FROM users WHERE user_id = ? AND password = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      // Hash input password then check if exists in database
      Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
      String hashedPwd = argon2.hash(Argon2Config.iterations,
              Argon2Config.memory, Argon2Config.parallelism, password.toCharArray());
      stmt.setString(2, hashedPwd);

      ResultSet rs = stmt.executeQuery();
      return rs.next();

//      // Get hashed password then compare
//      String sql = "SELECT password FROM users WHERE user_id = ?";
//      PreparedStatement stmt = conn.prepareStatement(sql);
//      stmt.setString(1, userId);
//      ResultSet rs = stmt.executeQuery();
//      if (rs.next()) {
//        String hashedPwd = rs.getString("password");
//        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
//        return argon2.verify(hashedPwd, password.toCharArray());
//      } else {
//        // Couldn't find corresponding user
//        return false;
//      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public String getFullName(String userId) {
    if (!connected || conn == null || userId == null) {
      return null;
    }

    try {
      String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        if (firstName != null && lastName != null) {
          return firstName + " " + lastName;
        } else if (firstName != null) {
          return firstName;
        } else if (lastName != null) {
          return lastName;
        } else {
          return "";
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return "";
  }

  @Override
  public JSONObject getRestaurantsById(String businessId, boolean visiting) {
    if (!connected || conn == null || businessId == null) {
      return null;
    }

    String sql = "SELECT * FROM restaurants where business_id = ?";
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, businessId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        Restaurant restaurant = new Restaurant.RestaurantBuilder()
                                      .setBusinessId(rs.getString("business_id"))
                                      .setName(rs.getString("name"))
                                      .setCategories(rs.getString("categories"))
                                      .setCity(rs.getString("city"))
                                      .setState(rs.getString("state"))
                                      .setStars(rs.getFloat("stars"))
                                      .setAddress(rs.getString("address"))
                                      .setLatitude(rs.getFloat("latitude"))
                                      .setLongitude(rs.getFloat("longitude"))
                                      .setUrl(rs.getString("url"))
                                      .setImageUrl(rs.getString("image_url"))
                                      .build();
        return restaurant.toJSONObject().put("visited", visiting);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
    if (!connected || conn == null || userId == null || businessIds == null) {
      return false;
    }

    String sqlHistory = "INSERT INTO history (user_id, business_id) VALUES (?,?)";
    String sqlUser = "SELECT 1 FROM users WHERE user_id=?";
    String sqlRestaurant = "SELECT 1 FROM restaurants WHERE business_id=?";
    try {
      PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
      stmtUser.setString(1, userId);
      ResultSet rs1 = stmtUser.executeQuery();
      if (!rs1.next()) {
        return false;
      }

      PreparedStatement stmtHistory = conn.prepareStatement(sqlHistory);
      PreparedStatement stmtRestaurant = conn.prepareStatement(sqlRestaurant);
      stmtHistory.setString(1, userId);
      for  (String businessId : businessIds) {
        // check if restaurant associated with businessId exists in database
        stmtRestaurant.setString(1, businessId);
        ResultSet rs2 = stmtRestaurant.executeQuery();

        // if restaurant exists, add new history
        if (rs2.next()) {
          stmtHistory.setString(2, businessId);
          stmtHistory.execute();
        }
      }
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
    if (!connected || conn == null || userId == null || businessIds == null) {
      return false;
    }

    String sql = "DELETE FROM history WHERE user_id=? AND business_id=?";
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, userId);
      for  (String businessId : businessIds) {
        statement.setString(2, businessId);
        statement.execute();
      }
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public Set<String> getVisitedRestaurants(String userId) {
    Set<String> visitedRestaurants = new HashSet<>();

    if (!connected || conn == null || userId == null) {
      return visitedRestaurants;
    }

    String sql = "SELECT business_id FROM history WHERE user_id = ?";
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, userId);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        visitedRestaurants.add(rs.getString("business_id"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return visitedRestaurants;
  }

  @Override
  public Set<String> getCategories(String businessId) {
    Set<String> categorySet = new HashSet<>();

    if (!connected || conn == null || businessId == null) {
      return categorySet;
    }

    String sql = "SELECT categories FROM restaurants WHERE business_id = ?";
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, businessId);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        String[] categories = rs.getString("categories").split(",");
        Collections.addAll(categorySet, categories);
      }
      return categorySet;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return categorySet;
  }

  @Override
  public Set<String> getBusinessIdByCategory(String category) {
    Set<String> businesses = new HashSet<>();
    if (!connected || conn == null || category == null) {
      return businesses;
    }

    String sql = "SELECT business_id FROM restaurants WHERE categories LIKE ?";
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, "%" + category + "%");
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        businesses.add(rs.getString("business_id"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return businesses;
  }

  @Override
  public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
    if (!connected || conn == null || userId == null) {
      return null;
    }

    YelpAPI yelpApi = new YelpAPI();
    JSONObject result = new JSONObject(yelpApi.searchBusinessByLocation(lat, lon, term));

    if (result.isNull("businesses")) {
      return null;
    }

    try {
      JSONArray businesses = (JSONArray) result.get("businesses");
      JSONArray purifiedBusinesses = new JSONArray();
      Set<String> visited = getVisitedRestaurants(userId);
      if (visited == null) {
        visited = new HashSet<>();
      }

      for (int i = 0; i < businesses.length(); i++) {
        Restaurant restaurant = new Restaurant.RestaurantBuilder(businesses.getJSONObject(i)).build();

        // If restaurant is already present in our db, won't be added
        String sql = "INSERT IGNORE INTO restaurants VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, restaurant.getBusinessId());
        statement.setString(2, restaurant.getName());
        statement.setString(3, restaurant.getCategories());
        statement.setString(4, restaurant.getCity());
        statement.setString(5, restaurant.getState());
        statement.setDouble(6, restaurant.getStars());
        statement.setString(7, restaurant.getAddress());
        statement.setDouble(8, restaurant.getLatitude());
        statement.setDouble(9, restaurant.getLongitude());
        statement.setString(10, restaurant.getUrl());
        statement.setString(11, restaurant.getImageUrl());
        statement.execute();

        // Filter if term is specified
        if (term == null || term.isEmpty() || restaurant.getCategories().contains(term)
                || restaurant.getName().contains(term) || restaurant.getAddress().contains(term)) {
          purifiedBusinesses.put(restaurant.toJSONObject().put("visited", visited.contains(restaurant.getBusinessId())));
        }
      }
      return purifiedBusinesses;
    } catch (JSONException | SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void main(String[] args) {
    MysqlDBConnection db = new MysqlDBConnection();
    System.out.println(db.searchRestaurants("ddhee", 37.38, -122.08, "coffee").toString());
  }
}
