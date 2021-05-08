package org.ddhee.Sniffer.rpc;

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

// Mark restaurants as visited
@WebServlet(name = "VisitHistory", value = "/history")
public class VisitHistory extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JSONObject input = RpcParser.readJsonObject(request);
    if (input == null || !input.has("user_id") || !input.has("visited")) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
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
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_OK);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}