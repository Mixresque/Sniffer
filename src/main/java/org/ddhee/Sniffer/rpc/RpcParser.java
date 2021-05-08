package org.ddhee.Sniffer.rpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RpcParser {
  private static void writeOutput(HttpServletResponse response, int code, Object obj) {
    try {
      response.setContentType("application/json");
      response.addHeader("Access-Control-Allow-Origin", "*");
      response.setStatus(code);
      PrintWriter out = response.getWriter();
      if (obj != null) {
        out.print(obj);
      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeJsonObject(HttpServletResponse response, int code, JSONObject obj) {
    writeOutput(response, code, obj);
  }

  public static void writeJsonArray(HttpServletResponse response, int code, JSONArray arr) {
    writeOutput(response, code, arr);
  }

  public static void writeEmptyResponse(HttpServletResponse response, int code) {
    writeOutput(response, code, null);
  }

  public static JSONObject readJsonObject(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader in = request.getReader();
      String line;
      while ((line = in.readLine()) != null) {
        sb.append(line);
      }
      in.close();
      return new JSONObject((sb.toString()));
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
    return null;
  }
}
