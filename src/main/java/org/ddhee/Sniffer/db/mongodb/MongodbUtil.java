package org.ddhee.Sniffer.db.mongodb;

public class MongodbUtil {
  private static final String HOST = "localhost";
  private static final String PORT = "27017";
  public static final String DB_NAME = "sniffer";
  private static final String USERNAME = "sniffer";
  private static final String PASSWORD = "sniffer";
  private static final String CONN_CONFIGS = "&ssl=true";
  public static final String DB_URI = "mongodb://" + USERNAME + ":" + PASSWORD
          + "@" + HOST + ":" + PORT; // + "/?authSource=" + DB_NAME + CONN_CONFIGS;
}
