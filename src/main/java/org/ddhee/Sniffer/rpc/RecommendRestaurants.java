package org.ddhee.Sniffer.rpc;

import org.ddhee.Sniffer.algorithm.Recommendation;
import org.json.JSONArray;

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
      // String userId = (String) session.getAttribute("user");
      String userId = request.getParameter("user_id");

      if (userId == null) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
      }

      JSONArray restaurants = Recommendation.recommendRestaurants(userId);

      if (restaurants == null) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

      RpcParser.writeJsonArray(response, HttpServletResponse.SC_OK, restaurants);
    }
}
