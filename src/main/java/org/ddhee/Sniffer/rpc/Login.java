package org.ddhee.Sniffer.rpc;

import org.ddhee.Sniffer.db.DBConnection;
import org.ddhee.Sniffer.db.DBConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "Login", value = "/login")
public class Login extends HttpServlet {
  private static DBConnection connection = DBConnectionFactory.getConnection();
  private static final int DEFAULT_SESSION_TIMEOUT = 10 * 60; // 10 minutes

  // Verify if the current session is logged in
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      HttpSession session = request.getSession();
      JSONObject msg = new JSONObject();
      if (session.getAttribute("user_id") == null) {
        RpcParser.writeJsonObject(response, HttpServletResponse.SC_FORBIDDEN, msg.put("status", "Session Invalid"));
      } else {
        String userId = (String) session.getAttribute("user_id");
        msg.put("user_id", userId);
        if (!connection.isConnected()) {
          connection = DBConnectionFactory.getConnection();
        }
        msg.put("name", connection.getFullName(userId));
        RpcParser.writeJsonObject(response, HttpServletResponse.SC_OK, msg.put("status", "OK"));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  // Log in a session
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String userId = request.getParameter("user_id");
    String password = request.getParameter("password");
    if (userId == null || password == null) {
      RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_BAD_REQUEST);
    }

    try {
      JSONObject msg = new JSONObject();
      if (!connection.isConnected()) {
        connection = DBConnectionFactory.getConnection();
      }
      if (!connection.verifyLogin(userId, password)) {
        RpcParser.writeEmptyResponse(response, HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        HttpSession session = request.getSession();
        // store user_id in session to mark current session as logged in
        session.setAttribute("user_id", userId);
        session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);
        msg.put("status", "OK");
        msg.put("user_id", userId);
        msg.put("name", connection.getFullName(userId));
        RpcParser.writeJsonObject(response, HttpServletResponse.SC_OK, msg);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
