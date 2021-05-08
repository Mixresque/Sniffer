package org.ddhee.Sniffer.db.mysql;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.entity.Restaurant;
import org.ddhee.Sniffer.external.YelpAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

public class MySQLDBConnection implements DBConnection {
  @Override
  public void close() {

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
    return null;
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
      for (int i = 0; i < businesses.length(); i++) {
        Restaurant restaurant = new Restaurant.RestaurantBuilder(businesses.getJSONObject(i)).build();
        purifiedBusinesses.put(restaurant.toJSONObject());
      }
      return purifiedBusinesses;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void main(String[] args) {
    MySQLDBConnection db = new MySQLDBConnection();
    System.out.println(db.searchRestaurants("ddhee", 37.38, -122.08, "coffee").toString());
  }
}
