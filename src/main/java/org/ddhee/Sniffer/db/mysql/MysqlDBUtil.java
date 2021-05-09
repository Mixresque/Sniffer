package org.ddhee.Sniffer.db.mysql;

public class MysqlDBUtil {
  private static final String HOST = "localhost";
  private static final String PORT = "3306";
  private static final String DB_NAME = "sniffer";
  private static final String USERNAME = "sniffer";
  private static final String PASSWORD = "sniffer";
  private static final String CONN_CONFIGS = "&autoReconnect=true&serverTimezone=UTC";
  public static final String DB_URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DB_NAME
                        + "?user=" + USERNAME + "&password=" + PASSWORD + CONN_CONFIGS;
//  public static final String DRIVER = "org.mariadb.jdbc.Driver";
}
