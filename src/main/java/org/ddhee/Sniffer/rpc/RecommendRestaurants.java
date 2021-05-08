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

// Recommend restaurants based on user history
@WebServlet(name = "RecommendRestaurants", value = "/recommendation")
public class RecommendRestaurants extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String userId = request.getParameter("user_id");
      if (userId == null) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      } else {
        try {
          JSONArray restaurants = new JSONArray();
          restaurants.put(new JSONObject()
                                .put("name", "Vegetizer")
                                .put("location", "Avenue Street"));
          restaurants.put(new JSONObject()
                                .put("name", "Vegetizer")
                                .put("location", "Avenue Street"));
          RpcParser.writeJsonArray(response, HttpServletResponse.SC_OK, restaurants);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
}
