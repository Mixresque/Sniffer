package org.ddhee.Sniffer.db.mongodb;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.bson.Document;
import org.ddhee.Sniffer.db.Argon2Config;


public class MongodbCreation {
  public static void main(String[] args) {
    MongoClient mongoClient = new MongoClient(new MongoClientURI(MongodbUtil.DB_URI));
    MongoDatabase db = mongoClient.getDatabase(MongodbUtil.DB_NAME);

    // Reset collections
    db.getCollection("users").drop();

    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    String password = "123";
    String hashedPwd = argon2.hash(Argon2Config.iterations,
            Argon2Config.memory, Argon2Config.parallelism, password.toCharArray());

    // Insert a fake user for testing
    db.getCollection("users").insertOne(
            new Document().append("user_id", "ddhee")
                          .append("password", hashedPwd)
                          .append("first_name", "Dd")
                          .append("last_name", "Hee")
    );

    // set index for collections
    // index options to keep user_id and business_id unique
    // 1 for ascending index; -1 for descending index
    IndexOptions indexOptions = new IndexOptions().unique(true);
    db.getCollection("users").createIndex(new Document("user_id", 1), indexOptions);
    db.getCollection("restaurants").createIndex(new Document("business_id", 1), indexOptions);

    // use a compound text index for searching restaurants
    db.getCollection("restaurants").createIndex(
            new Document().append("categories", "text")
                          .append("name", "text")
                          .append("address", "text")
    );

    mongoClient.close();
  }
}
