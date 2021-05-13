package org.ddhee.Sniffer.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {
  static private Restaurant testRestaurant;

  @BeforeAll
  static void setUp() {
    testRestaurant = new Restaurant.RestaurantBuilder().setName("Gertrude's").setBusinessId("x0rZunJEE9AQtXySoVwL1Q")
            .setCategories("American (New),Breakfast & Brunch,Venues & Event Spaces").setStars(4.0).setCity("Baltimore")
            .setLatitude(39.3262187433177).setLongitude(-76.6191216172726).setAddress("10 Art Museum Dr, Baltimore, MD 21218")
            .setState("MD").setImageUrl("https://s3-media1.fl.yelpcdn.com/bphoto/k_Fb129DpAmrHH68ydydAg/o.jpg")
            .setUrl("https://www.yelp.com/biz/gertrudes-baltimore?adjust_creative=BtxfRGWabRQAGLnK4LxecQ&utm_campaign=yelp_api" +
                    "_v3&utm_medium=api_v3_business_search&utm_source=BtxfRGWabRQAGLnK4LxecQ").build();
  }

  @AfterEach
  void tearDown(TestInfo testInfo) {
    System.out.println("Test completed: " + testInfo.getDisplayName());
  }

  @Test
  void testJsonArrayToString() {
    Restaurant.RestaurantBuilder builder = new Restaurant.RestaurantBuilder();
    JSONArray jsonArray = new JSONArray();
    jsonArray.put("Mexican").put("Italian").put("Caffe");
    assertEquals("Mexican,Italian,Caffe", builder.jsonArrayToString(jsonArray));
  }

  @Test
  void testJsonArrayToStringCornerCases() {
    Restaurant.RestaurantBuilder builder = new Restaurant.RestaurantBuilder();
    JSONArray jsonArray = new JSONArray();
    jsonArray.put("").put("Italian").put("");
    assertEquals(",Italian,", builder.jsonArrayToString(jsonArray));

    jsonArray = new JSONArray().put("");
    assertEquals("", builder.jsonArrayToString(jsonArray));

    jsonArray = new JSONArray().put("  ");
    assertEquals("  ", builder.jsonArrayToString(jsonArray));

    jsonArray = new JSONArray().put("Mexican");
    assertEquals("Mexican", builder.jsonArrayToString(jsonArray));
  }

  @Test
  void testJSONObjectToRestaurant() {
    String jsonStr = "{\"id\": \"x0rZunJEE9AQtXySoVwL1Q\", \"alias\": \"gertrudes-baltimore\", \"name\": \"Gertrude's\"," +
            " \"image_url\": \"https://s3-media1.fl.yelpcdn.com/bphoto/k_Fb129DpAmrHH68ydydAg/o.jpg\", \"is_closed\": false," +
            " \"url\": \"https://www.yelp.com/biz/gertrudes-baltimore?adjust_creative=BtxfRGWabRQAGLnK4LxecQ&utm_campaign=" +
            "yelp_api_v3&utm_medium=api_v3_business_search&utm_source=BtxfRGWabRQAGLnK4LxecQ\", \"review_count\": 383, " +
            "\"categories\": [{\"alias\": \"newamerican\", \"title\": \"American (New)\"}, {\"alias\": \"breakfast_brunch\", " +
            "\"title\": \"Breakfast & Brunch\"}, {\"alias\": \"venues\", \"title\": \"Venues & Event Spaces\"}], \"rating\": 4.0, " +
            "\"coordinates\": {\"latitude\": 39.3262187433177, \"longitude\": -76.6191216172726}, \"transactions\": [\"delivery\"], " +
            "\"price\": \"$$\", \"location\": {\"address1\": \"10 Art Museum Dr\", \"address2\": \"\", \"address3\": \"\", \"city\": " +
            "\"Baltimore\", \"zip_code\": \"21218\", \"country\": \"US\", \"state\": \"MD\", \"display_address\": [\"10 Art Museum Dr\"," +
            "\" Baltimore, MD 21218\"]}, \"phone\": \"+14108893399\", \"display_phone\": \"(410) 889-3399\", \"distance\": 273.2056274865919}";

    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject = new JSONObject(jsonStr);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Restaurant restaurant = new Restaurant.RestaurantBuilder(jsonObject).build();
    assertEquals(testRestaurant.getUrl(), restaurant.getUrl());
    assertEquals(testRestaurant.getAddress(), restaurant.getAddress());
    assertEquals(testRestaurant.getBusinessId(), restaurant.getBusinessId());
    assertEquals(testRestaurant.getCategories(), restaurant.getCategories());
    assertEquals(testRestaurant.getCity(), restaurant.getCity());
    assertEquals(testRestaurant.getLatitude(), restaurant.getLatitude());
    assertEquals(testRestaurant.getLongitude(), restaurant.getLongitude());
    assertEquals(testRestaurant.getImageUrl(), restaurant.getImageUrl());
    assertEquals(testRestaurant.getStars(), restaurant.getStars());
    assertEquals(testRestaurant.getState(), restaurant.getState());
  }
}