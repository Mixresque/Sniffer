package org.ddhee.Sniffer.rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.db.DBConnectionFactory;
import org.json.JSONArray;

// Search restaurants by geo location
@WebServlet(name = "SearchRestaurants", value = "/restaurants")
public class SearchRestaurants extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    if (session.getAttribute("user_id") == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String lat = request.getParameter("lat");
    String lon = request.getParameter("lon");

    JSONArray restaurants = new JSONArray();

    if (lat != null && lon != null) {
      String userId = (String) session.getAttribute("user_id");

      // term is null or empty by default
      String term = request.getParameter("term");

      DBConnection connection = DBConnectionFactory.getConnection();

      if (!connection.isConnected()) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

      restaurants = connection.searchRestaurants(userId, Double.parseDouble(lat), Double.parseDouble(lon), term);
      connection.close();
      RpcParser.writeJsonArray(response, HttpServletResponse.SC_OK, restaurants);
    }
  }
}