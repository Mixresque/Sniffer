package org.ddhee.Sniffer.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
  private String businessId;
  private String name;
  private String categories;
  private String city;
  private String state;
  private String address;
  private double stars;
  private double latitude;
  private double longitude;
  private String imageUrl;
  private String url;

  private Restaurant(RestaurantBuilder builder) {
    this.businessId = builder.businessId;
    this.name = builder.name;
    this.categories = builder.categories;
    this.city = builder.city;
    this.state = builder.state;
    this.address = builder.address;
    this.stars = builder.stars;
    this.latitude = builder.latitude;
    this.longitude = builder.longitude;
    this.imageUrl = builder.imageUrl;
    this.url = builder.url;
  }

  public static class RestaurantBuilder {
    private String businessId;
    private String name;
    private String categories;
    private String city;
    private String state;
    private String address;
    private double stars;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String url;

    public RestaurantBuilder() {
    }

    public RestaurantBuilder(JSONObject obj) {
      if (obj == null) {
        return;
      }

      try {
        this.businessId = obj.getString("id");
        this.name = obj.getString("name");
        this.imageUrl = obj.getString("image_url");
        this.url = obj.getString("url");
        this.stars = obj.getDouble("rating");

        JSONArray categories = (JSONArray) obj.get("categories");
        List<String> categoryList = new ArrayList<>();
        for (int i = 0; i < categories.length(); i++) {
          JSONObject category = categories.getJSONObject(i);
          categoryList.add(category.getString("title"));
        }
        this.categories = String.join(",", categoryList);

        JSONObject coordinates = (JSONObject) obj.get("coordinates");
        this.latitude = coordinates.getDouble("latitude");
        this.longitude = coordinates.getDouble("longitude");

        JSONObject location = (JSONObject) obj.get("location");
        this.city = location.getString("city");
        this.state = location.getString("state");
        this.address = jsonArrayToString((JSONArray) location.get("display_address"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    public Restaurant build() {
      return new Restaurant(this);
    }

    // helper function to convert a json array to string, connecting objects with ", "
    String jsonArrayToString(JSONArray array) {
      if (array == null) {
        return null;
      }
      StringBuilder sb = new StringBuilder();
      try {
        for (int i = 0; i < array.length(); i++) {
          String obj = (String) array.get(i);
          sb.append(obj);
          if (i != array.length() - 1) {
            sb.append(",");
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return sb.toString();
    }

    public RestaurantBuilder setBusinessId(String businessId) {
      this.businessId = businessId;
      return this;
    }

    public RestaurantBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public RestaurantBuilder setCategories(String categories) {
      this.categories = categories;
      return this;
    }

    public RestaurantBuilder setCity(String city) {
      this.city = city;
      return this;
    }

    public RestaurantBuilder setState(String state) {
      this.state = state;
      return this;
    }

    public RestaurantBuilder setAddress(String address) {
      this.address = address;
      return this;
    }

    public RestaurantBuilder setStars(double stars) {
      this.stars = stars;
      return this;
    }

    public RestaurantBuilder setLatitude(double latitude) {
      this.latitude = latitude;
      return this;
    }

    public RestaurantBuilder setLongitude(double longitude) {
      this.longitude = longitude;
      return this;
    }

    public RestaurantBuilder setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public RestaurantBuilder setUrl(String url) {
      this.url = url;
      return this;
    }
  }

  // helper function to clean up data returned from Yelp API
  private String parseString(String str) {
    if (str == null) {
      return null;
    }
    return str.replace("\"", "\\\"").replace("/", " or ");
  }

  // helper function to convert a json array to string, after cleaning up data
  private JSONArray stringToJSONArray(String str) {
    if (str == null) {
      return null;
    }
    try {
      return new JSONArray("[" + parseString((str) + "]"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject();
    try {
      obj.put("business_id", businessId);
      obj.put("name", name);
      obj.put("categories", stringToJSONArray(categories));
      obj.put("city", city);
      obj.put("state", state);
      obj.put("address", address);
      obj.put("stars", stars);
      obj.put("latitude", latitude);
      obj.put("longitude", longitude);
      obj.put("image_url", imageUrl);
      obj.put("url", url);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return obj;
  }

  public String getBusinessId() {
    return businessId;
  }

  public String getName() {
    return name;
  }

  public String getCategories() {
    return categories;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getAddress() {
    return address;
  }

  public double getStars() {
    return stars;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getUrl() {
    return url;
  }

  public static void main(String[] args) {
    Restaurant e = new Restaurant.RestaurantBuilder().setName("asdf").setStars(1).build();
    System.out.println(e.getStars());
  }
}
