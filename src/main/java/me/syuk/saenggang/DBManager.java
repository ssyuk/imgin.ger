package me.syuk.saenggang;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.javacord.api.entity.user.User;

import java.time.LocalDate;
import java.util.ArrayList;

import static me.syuk.saenggang.Main.properties;

public class DBManager {
    private static MongoCollection<Document> messageCollection;
    private static MongoCollection<Document> accountCollection;
    private static MongoCollection<Document> attendanceCollection;

    public static void connect() {
        String connectionString = "mongodb+srv://syuk:" + properties.getProperty("DB_PASSWORD") + "@saenggang.dm5clne.mongodb.net/?retryWrites=true&w=majority";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase saenggangDB = mongoClient.getDatabase("saenggang");
        messageCollection = saenggangDB.getCollection("message");
        accountCollection = saenggangDB.getCollection("account");
        attendanceCollection = saenggangDB.getCollection("attendance");

    }

    public static SaenggangKnown getKnown(String command) {
        Document document = messageCollection.find(new Document("question", command)).first();
        if (document == null) return null;

        return new SaenggangKnown(
                document.getString("question"),
                document.getString("answer"),
                document.getString("authorName"),
                document.getString("authorId")
        );
    }

    public static void addKnown(SaenggangKnown message) {
        Document document = new Document("question", message.question())
                .append("answer", message.answer())
                .append("authorName", message.authorName())
                .append("authorId", message.authorId());

        messageCollection.insertOne(document);
    }

    public static void removeKnown(SaenggangKnown message) {
        Document document = new Document("question", message.question())
                .append("answer", message.answer())
                .append("authorName", message.authorName())
                .append("authorId", message.authorId());

        messageCollection.deleteOne(document);
    }

    public static Account getAccount(User user) {
        Document document = accountCollection.find(new Document("userId", user.getIdAsString())).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", user.getIdAsString()).append("point", 0));
            return new Account(user.getIdAsString(), 0);
        }

        return new Account(
                document.getString("userId"),
                document.getInteger("point")
        );
    }

    public static void givePoint(Account account, int point) {
        Document document = accountCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", account.userId()).append("point", 0));
            document = accountCollection.find(new Document("userId", account.userId())).first();
        }

        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", new Document("point", document.getInteger("point") + point)));
    }

    public static boolean isAttended(Account account) {
        LocalDate now = LocalDate.now();

        Document document = attendanceCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            attendanceCollection.insertOne(
                    new Document("userId", account.userId())
                            .append("attendMap", new ArrayList<>())
            );
            return false;
        }

        return document.getList("attendMap", String.class).contains(now.toString());
    }

    public static void attend(Account account) {
        LocalDate now = LocalDate.now();

        Document document = attendanceCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            attendanceCollection.insertOne(
                    new Document("userId", account.userId())
                            .append("attendMap", new Document())
            );
            document = attendanceCollection.find(new Document("userId", account.userId())).first();
        }

        document.getList("attendMap", String.class).add(now.toString());
        attendanceCollection.updateOne(new Document("userId", account.userId()), new Document("$set", document));
    }
}
