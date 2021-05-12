package org.ddhee.Sniffer.rpc;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.db.DBConnectionFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Mark restaurants as visited
@WebServlet(name = "VisitHistory", value = "/history")
public class VisitHistory extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    if (session.getAttribute("user_id") == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    JSONObject input = RpcParser.readJsonObject(request);
    if (input == null || !input.has("visited")) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = DBConnectionFactory.getConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      String userId = (String) session.getAttribute("user_id");
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
    HttpSession session = request.getSession();
    if (session.getAttribute("user_id") == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    JSONObject input = RpcParser.readJsonObject(request);
    if (input == null || !input.has("visited")) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = DBConnectionFactory.getConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      String userId = (String) session.getAttribute("user_id");
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
    HttpSession session = request.getSession();
    if (session.getAttribute("user_id") == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_FORBIDDEN);
      return;
    }

     String userId = (String) session.getAttribute("user_id");

    if (userId == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DBConnection connection = DBConnectionFactory.getConnection();
    if (!connection.isConnected()) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      Set<String> visitedRestaurantIds = connection.getVisitedRestaurants(userId);
      JSONArray visitedRestaurants = new JSONArray();
      for (String restaurantId : visitedRestaurantIds) {
        visitedRestaurants.put(connection.getRestaurantById(restaurantId, true));
      }
      RpcParser.writeJsonArray(response, HttpServletResponse.SC_OK, visitedRestaurants);
    } catch (JSONException e) {
      e.printStackTrace();
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}