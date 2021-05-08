package org.ddhee.Sniffer.external;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YelpAPI {
  private static final String API_HOST = "https://api.yelp.com";
  private static final String SEARCH_PATH = "/v3/businesses/search";
  private static final String TOKEN_HOST = "https://api.yelp.com/oauth2/token";
  private static final String CLIENT_ID = "BtxfRGWabRQAGLnK4LxecQ";
  private static final String CLIENT_SECRET = "gEvhAai5KSKpqpBFffxW7MtvAXmkPtAmJZqIG4YoqKNP8zjVs__itDakQ61cqS-2dgg2_KaXxhJcxZeeRDu9mG-VbZ2bsHltjBgBYDfYNG1eMj2A319_EvqIEsuWYHYx";
  private static final String GRANT_TYPE = "client_credentials";
  private static final String TOKEN_TYPE = "Bearer";
  private static final String DEFAULT_TERM = "coffee";
  private static final int SEARCH_LIMIT = 20;

  public YelpAPI() {}

  // Prior to December 7, 2017 Yelp API used OAuth 2.0 to authenticate requests to the API
  // Not needed now
  // Creates and sends request to Yelp Token and return the access token
  private JSONObject obtainAccessToken() {
    try {
      String query = String.format("grant_type=%s&client_id=%s&client_secret=%s",
                                      GRANT_TYPE,
                                      CLIENT_ID,
                                      CLIENT_SECRET);
      HttpURLConnection connection = (HttpURLConnection) new URL(TOKEN_HOST).openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      // add request header
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.write(query.getBytes());

      System.out.println("\nSending 'POST' request to URL : " + TOKEN_HOST);
      int responseCode = connection.getResponseCode();
      System.out.println("Response Code : " + responseCode);

      // read response into a JSONObject
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      return new JSONObject(response.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String searchBusinessByLocation(double lat, double lon) {
    return searchBusinessByLocation(lat, lon, DEFAULT_TERM);
  }

  // Creates and sends request to the Business Search API by location
  public String searchBusinessByLocation(double lat, double lon, String term) {
    String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s",
                                  term, lat, lon, SEARCH_LIMIT);
    String url = API_HOST + SEARCH_PATH;

//    JSONObject accessTokenOutput = obtainAccessToken();
//    if (accessTokenOutput == null) {
//      return null;
//    }
//    String accessToken = accessTokenOutput.getString("access_token");

    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();

      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");

      // connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + accessToken);
      connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + CLIENT_SECRET);

      System.out.println("\nSending 'GET' request to URL : " + url);
      int responseCode = connection.getResponseCode();
      System.out.println("Response Code : " + responseCode);

      BufferedReader in = new BufferedReader(new
              InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      return response.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  // Queries Yelp Search API based on input arguments
  // and verify the returned results
  // for debugging purpose
  private void queryAPI(double lat, double lon) {
    String searchResult = searchBusinessByLocation(lat, lon);
    if (searchResult == null) {
      return;
    }

    try {
      JSONObject searchResultJson = new JSONObject(searchResult);
      JSONArray buisinesses = (JSONArray) searchResultJson.get("businesses");
      for (int i = 0; i < searchResultJson.length(); i++) {
        JSONObject business = (JSONObject) buisinesses.get(i);
        System.out.println(business);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  // Queries Yelp Search API based on command line arguments
  public static void main(String[] args) {
    YelpAPI yelpApi = new YelpAPI();
    yelpApi.queryAPI(37.38, -122.08);
  }
}
