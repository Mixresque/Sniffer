package org.ddhee.Sniffer.rpc;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.db.mysql.MysqlDBConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Mark restaurants as visited
@WebServlet(name = "VisitHistory", value = "/history")
public class VisitHistory extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JSONObject input = RpcParser.readJsonObject(request);
    if (input == null || !input.has("user_id") || !input.has("visited")) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = new MysqlDBConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      String userId = input.getString("user_id");
      JSONArray visited = (JSONArray) input.get("visited");
      List<String> visitedRestaurants = new ArrayList<>();
      for (int i = 0; i < visited.length(); i++) {
        String restaurantId = (String) visited.get(i);
        visitedRestaurants.add(restaurantId);
      }

      if (connection.setVisitedRestaurants(userId, visitedRestaurants)) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_OK);
      } else {
        // user_id or business_id not found
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (JSONException e) {
      e.printStackTrace();
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JSONObject input = RpcParser.readJsonObject(request);
    if (input == null || !input.has("user_id") || !input.has("visited")) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = new MysqlDBConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      String userId = input.getString("user_id");
      JSONArray visited = (JSONArray) input.get("visited");
      List<String> visitedRestaurants = new ArrayList<>();
      for (int i = 0; i < visited.length(); i++) {
        String restaurantId = (String) visited.get(i);
        visitedRestaurants.add(restaurantId);
      }

      if (connection.unsetVisitedRestaurants(userId, visitedRestaurants)) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_OK);
      } else {
        // user_id or business_id not found
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (JSONException e) {
      e.printStackTrace();
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String userId = request.getParameter("user_id");
    // String userId = (String) session.getAttribute("user");

    if (userId == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = new MysqlDBConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      Set<String> visitedRestaurants = connection.getVisitedRestaurants(userId);
      JSONArray visited = new JSONArray();
      for (String restaurantId : visitedRestaurants) {
        visited.put(restaurantId);
      }
      RpcParser.writeJsonArray(response, HttpServletResponse.SC_OK, visited);
    } catch (JSONException e) {
      e.printStackTrace();
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}