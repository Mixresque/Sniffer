package org.ddhee.Sniffer.db.mongodb;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.bson.Document;
import org.ddhee.Sniffer.db.Argon2Config;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MongodbCreation {
  public static void main(String[] args) {
    MongoClient mongoClient = new MongoClient(new MongoClientURI(MongodbUtil.DB_URI));
    MongoDatabase db = mongoClient.getDatabase(MongodbUtil.DB_NAME);

    // Reset collections
    db.getCollection("users").drop();

    String userId = "user";
    String password = "123";
    String firstName = "John";
    String lastName = "Smith";

    // compute md5(userId + md5(password))
    String md5Hashed = password;
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] messageDigest = md5.digest(password.getBytes());
      BigInteger no = new BigInteger(1, messageDigest);
      md5Hashed = no.toString(16);
      while (md5Hashed.length() < 32) {
        md5Hashed = "0" + md5Hashed;
      }

      md5Hashed = userId + md5Hashed;
      messageDigest = md5.digest(md5Hashed.getBytes());
      no = new BigInteger(1, messageDigest);
      md5Hashed = no.toString(16);
      while (md5Hashed.length() < 32) {
        md5Hashed = "0" + md5Hashed;
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    String hashedPwd = argon2.hash(Argon2Config.iterations,
            Argon2Config.memory, Argon2Config.parallelism, md5Hashed.toCharArray());

    // Insert a fake user for testing
    db.getCollection("users").insertOne(
            new Document().append("user_id", userId)
                          .append("password", hashedPwd)
                          .append("first_name", firstName)
                          .append("last_name", lastName)
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
