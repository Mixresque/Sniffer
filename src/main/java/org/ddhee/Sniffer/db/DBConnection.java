package org.ddhee.Sniffer.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

public interface DBConnection {
  /**
   * Close connection to database
   */
  public void close();

  /**
   * Get database connection status
   * @return database connection status
   */
  public boolean isConnected();

  /**
   * Verify if password is correct for a user id
   * @param userId - user id
   * @param password - password
   * @return if password is correct or not
   */
  public Boolean verifyLogin(String userId, String password);

  /**
   * Get user's first and last name for a user id
   * @param userId - queried user's id
   * @return full name of the user
   */
  public String getFullName(String userId);

  /**
   * Get a restaurant in JSON format by id;
   * if visited is true, set the visited field in json
   * @param businessId - queried restaurant's id
   * @param visiting - set the visited field in json if visiting
   * @return queried restaurant in JSON
   */
  public JSONObject getRestaurantsById(String businessId, boolean visiting);

  /**
   * Set a list of restaurants as visited for a user
   * @param userId - the user's id
   * @param businessIds - list of restaurant ids
   * @return if operation was successful
   */
  public boolean setVisitedRestaurants(String userId, List<String> businessIds);

  /**
   * Unset a list of restaurants as visited for a user
   * @param userId - the user's id
   * @param businessIds - list of restaurant ids
   * @return if operation was successful
   */
  public boolean unsetVisitedRestaurants(String userId, List<String> businessIds);

  /**
   * Get the set of visited restaurants for a user
   * @param userId - the user's id
   * @return set of visited restaurants' ids
   */
  public Set<String> getVisitedRestaurants(String userId);

  /**
   * Get the categories of a restaurant
   * @param businessId - restaurant's id
   * @return set of categories
   */
  public Set<String> getCategories(String businessId);

  /**
   * Get business ids by a category
   * @param category - queried category
   * @return set of business ids of businesses from input category
   */
  public Set<String> getBusinessIdByCategory(String category);

  /**
   * Search restaurants by keyword near a geolocation
   * @param userId - user's id
   * @param lat - latitude
   * @param lon - longitude
   * @param term - keyword
   * @return list of restaurants in JSON array
   */
  public JSONArray searchRestaurants(String userId, double lat, double lon, String term);
}
