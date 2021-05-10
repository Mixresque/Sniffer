package org.ddhee.Sniffer.db;

import org.ddhee.Sniffer.db.mongodb.MongodbConnection;
import org.ddhee.Sniffer.db.mysql.MysqlDBConnection;

public class DBConnectionFactory {
  private static final String DEFAULT_DB = "mysql";

  public static DBConnection getConnection(String db) {
    switch (db) {
      case "mysql":
        return MysqlDBConnection.getInstance();
      case "mongodb":
        return MongodbConnection.getInstance();
      default:
        throw new IllegalArgumentException("Invalid database name: " + db);
    }
  }

  public static DBConnection getConnection() {
    return getConnection(DEFAULT_DB);
  }
}
