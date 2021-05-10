package org.ddhee.Sniffer.db.mongodb;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.bson.Document;
import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.entity.Restaurant;
import org.ddhee.Sniffer.external.YelpAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mongodb.client.model.Filters.eq;

import java.util.*;
import java.util.stream.Collectors;

public class MongodbConnection implements DBConnection {
  private MongoClient mongoClient;
  private MongoDatabase db;
  private boolean connected;
  private static MongodbConnection instance;

  public static DBConnection getInstance() {
    if (instance == null || !instance.isConnected()) {
      instance = new MongodbConnection();
    }
    return instance;
  }

  private MongodbConnection(String dbURI, String dbName) {
    mongoClient = new MongoClient(new MongoClientURI(dbURI));
    db = mongoClient.getDatabase(dbName);
    connected = true;
  }

  private MongodbConnection() {
    this(MongodbUtil.DB_URI, MongodbUtil.DB_NAME);
  }

  @Override
  public void close() {
    if (mongoClient != null) {
      mongoClient.close();
    }
    connected = false;
  }

  @Override
  public boolean isConnected() {
    return connected && mongoClient != null && db != null;
  }

  @Override
  public Boolean verifyLogin(String userId, String password) {
    if (!isConnected() || userId == null || password == null) {
      return false;
    }

    FindIterable<Document> result = db.getCollection("users").find(
            eq("user_id", userId)
    );

    String hashedPwd = result.first().getString("password");
    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    return argon2.verify(hashedPwd, password.toCharArray());
  }

  @Override
  public String getFullName(String userId) {
    if (!isConnected() || userId == null) {
      return null;
    }

    FindIterable<Document> result = db.getCollection("users").find(
            eq("user_id", userId)
    );

    if (result.first().containsKey("first_name") && result.first().containsKey("last_name")) {
      return result.first().getString("first_name") + " " + result.first().getString("last_name");
    } else {
      return "";
    }
  }

  @Override
  public JSONObject getRestaurantById(String businessId, boolean visiting) {
    if (!isConnected() || businessId == null) {
      return null;
    }

    FindIterable<Document> result = db.getCollection("restaurants").find(
            eq("business_id", businessId)
    );

    try {
      JSONObject restaurant = new JSONObject(result.first().toJson());
      String categories = restaurant.getString("categories").replace("\"", "\\\"").replace("/", " or ");
      restaurant.put("categories", new JSONArray("[" + categories + "]"));
      restaurant.put("visited", visiting);
      return restaurant;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
    if (!isConnected() || userId == null || businessIds == null) {
      return false;
    }
    db.getCollection("users").updateOne(
            new Document("user_id", userId),
            new Document(
                    "$push",
                    new Document("visited", new Document("$each", businessIds))
            )
    );
    return true;
  }

  @Override
  public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
    if (!isConnected() || userId == null || businessIds == null) {
      return false;
    }
    db.getCollection("users").updateOne(
            new Document("user_id", userId),
            new Document(
                    "$pull",
                    new Document("visited", new Document("$in", businessIds)))
    );
    return true;
  }

  @Override
  public Set<String> getVisitedRestaurants(String userId) {
    Set<String> visitedRestaurants = new HashSet<>();

    if (!isConnected() || userId == null) {
      return visitedRestaurants;
    }

    FindIterable<Document> result = db.getCollection("users").find(
            eq("user_id", userId)
    );
    if (result.first().containsKey("visited")) {
      visitedRestaurants.addAll((List<String>) result.first().get("visited"));
    }

    return visitedRestaurants;
  }

  @Override
  public Set<String> getCategories(String businessId) {
    if (!isConnected() || businessId == null) {
      return new HashSet<>();
    }

    FindIterable<Document> result = db.getCollection("restaurants").find(
            eq("business_id", businessId)
    );
    if (result.first().containsKey("categories")) {
      String[] categories = result.first().getString("categories").split(",");
      return Arrays.stream(categories).map(String::trim).collect(Collectors.toSet());
    }

    return new HashSet<>();
  }

  @Override
  public Set<String> getBusinessIdByCategory(String category) {
    Set<String> businesses = new HashSet<>();
    if (!isConnected() || category == null) {
      return businesses;
    }

//    System.out.println(category.replace("(", "\\\\(").replace(")", "\\\\)"));
//
//    FindIterable<Document> result = db.getCollection("restaurants").find(
//            new Document("categories", new Document("$regex", category.replace("(", "\\\\(").replace(")", "\\\\)")))
//    );
//    regex("categories", category.replace("(", "\\\\(").replace(")", "\\\\)"))

    FindIterable<Document> result = db.getCollection("restaurants").find(
            new Document("categories", new Document("$regex", category.split("\\(")[0]))
    );

    result.forEach((Block<Document>) document -> businesses.add(document.getString("business_id")));
    return businesses;
  }

  @Override
  public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
    if (!isConnected() || userId == null) {
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

        UpdateOptions options = new UpdateOptions().upsert(true);
        db.getCollection("restaurants").updateOne(
                new Document().append("business_id", restaurant.getBusinessId()),
                new Document(
                        "$set",
                        new Document().append("business_id", restaurant.getBusinessId())
                                      .append("name", restaurant.getName())
                                      .append("categories", restaurant.getCategories())
                                      .append("city", restaurant.getCity())
                                      .append("state", restaurant.getState())
                                      .append("stars", restaurant.getStars())
                                      .append("address", restaurant.getAddress())
                                      .append("latitude", restaurant.getLatitude())
                                      .append("longitude", restaurant.getLongitude())
                                      .append("url", restaurant.getUrl())
                                      .append("image_url", restaurant.getImageUrl())
                ),
                options
        );

        // Filter if term is specified
        if (term == null || term.isEmpty() || restaurant.getCategories().contains(term)
                || restaurant.getName().contains(term) || restaurant.getAddress().contains(term)) {
          purifiedBusinesses.put(restaurant.toJSONObject().put("visited", visited.contains(restaurant.getBusinessId())));
        }
      }
      return purifiedBusinesses;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void main(String[] args) {
    DBConnection conn = MongodbConnection.getInstance();
    System.out.println(conn.getFullName("ddhee"));
  }
}
