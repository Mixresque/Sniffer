package org.ddhee.Sniffer.db.mysql;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.entity.Restaurant;
import org.ddhee.Sniffer.external.YelpAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MysqlDBConnection implements DBConnection {
  private Connection conn = null;
  private static final int MAX_RECOMMENDED_RESTAURANTS = 10;

  public MysqlDBConnection(String url) {
    try {
      Class.forName(MysqlDBUtil.DRIVER).newInstance();
      conn = DriverManager.getConnection(url);
    } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
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
    }
  }

  @Override
  public Boolean verifyLogin(String userId, String password) {
    return null;
  }

  @Override
  public String getFullName(String userId) {
    return null;
  }

  @Override
  public JSONObject getRestaurantsById(String businessId, boolean visited) {
    return null;
  }

  @Override
  public void setVisitedRestaurants(String userId, List<String> businessIds) {

  }

  @Override
  public void unsetVisitedRestaurants(String userId, List<String> businessIds) {

  }

  @Override
  public Set<String> getVisitedRestaurants(String userId) {
    if (userId == null) {
      return null;
    }

    Set<String> vistedRestaurants = new HashSet<>();

    try {
      String sql = "SELECT business_id FROM history WHERE user_id = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, userId);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        vistedRestaurants.add(rs.getString("business_id"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return vistedRestaurants;
  }

  @Override
  public JSONArray recommendRestaurants(String userId) {
    return null;
  }

  @Override
  public Set<String> getCategories(String businessId) {
    return null;
  }

  @Override
  public Set<String> getBusinessId(String category) {
    return null;
  }

  @Override
  public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
    YelpAPI yelpApi = new YelpAPI();
    JSONObject result = new JSONObject(yelpApi.searchBusinessByLocation(lat, lon));

    if (result.isNull("businesses")) {
      return null;
    }

    try {
      JSONArray businesses = (JSONArray) result.get("businesses");
      JSONArray purifiedBusinesses = new JSONArray();
      Set<String> visited = getVisitedRestaurants(userId);

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
