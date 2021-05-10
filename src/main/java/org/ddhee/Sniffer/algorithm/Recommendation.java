package org.ddhee.Sniffer.algorithm;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.db.DBConnectionFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


public class Recommendation {
  private static final int MAX_RECOMMENDED_RESTAURANTS = 10;

  /**
   * Recommend restaurants for a user
   * @param userId - user's id
   * @return list of recommended restaurants in JSON array
   */
  public static JSONArray recommendRestaurants(String userId) {
    DBConnection connection = DBConnectionFactory.getConnection();
    if (!connection.isConnected()) {
      return null;
    }

    /**
     * Current recommendation algorithm
     * Step 1: get all restaurants the user has visited
     * Step 2: get the categories of the visited restaurants
     * Step 3: find restaurants with similar categories, filtering
     *         out already visited restaurants
     * Step 4: get details about found restaurants with their ids,
     *         compose result JSONArray
     */

    // Step 1
    Set<String> visitedRestaurants = connection.getVisitedRestaurants(userId);

//    System.out.println("visted restaurants: ");
//    for (String id : visitedRestaurants) {
//      System.out.println(id);
//    }

    // Step 2.1 count categories of visited restaurants
    Map<String, Integer> categoryCounts = new HashMap<>();
    for (String restaurantId : visitedRestaurants) {
      Set<String> categories =  connection.getCategories(restaurantId);
      for (String category : categories) {
        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
      }
    }
    // Step 2.2 sort categories by counts
    List<Map.Entry<String, Integer>> categoryCountsList = new ArrayList<>(categoryCounts.entrySet());
    categoryCountsList.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

//    System.out.println("sorted category counts:");
//    for (Map.Entry<String, Integer> entry : categoryCountsList) {
//      System.out.println(entry.getKey() + ": " + entry.getValue());
//    }

    // Step 3.1 get ids of restaurants with same categories
    Set<String> recommendedRestaurantIds = new HashSet<>();
    for (Map.Entry<String, Integer> categoryAndCount : categoryCountsList) {
      Set<String> businesses = connection.getBusinessIdByCategory(categoryAndCount.getKey());

//      System.out.println("Recommending for: " + categoryAndCount.getKey());

      for (String businessId : businesses) {
        if (!visitedRestaurants.contains(businessId)) {
          recommendedRestaurantIds.add(businessId);

//          System.out.println(businessId);

          if (recommendedRestaurantIds.size() >= MAX_RECOMMENDED_RESTAURANTS) {
            break;
          }
        }
      }

      if (recommendedRestaurantIds.size() >= MAX_RECOMMENDED_RESTAURANTS) {
        break;
      }
    }

    // Step 3.2 TODO - request Yelp API to search near user last visit's location with top categories

    // Step 4 get recommended restaurants' info and store in JSONArray
    JSONArray recommendedRestaurants = new JSONArray();
    for (String restaurantId : recommendedRestaurantIds) {
      JSONObject restaurant = connection.getRestaurantById(restaurantId, false);
      if (restaurant != null) {
        recommendedRestaurants.put(restaurant);
      }
    }

    return recommendedRestaurants;
  }

}
