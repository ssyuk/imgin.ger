package me.syuk.saenggang.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import org.bson.Document;
import org.javacord.api.entity.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public static Document getUserDocument(String userId) {
        Document document = accountCollection.find(new Document("userId", userId)).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", userId).append("coin", 0));
            document = accountCollection.find(new Document("userId", userId)).first();
        }
        return document;
    }

    public static Account getAccount(User user) {
        Document document = getUserDocument(user.getIdAsString());
        if (document == null) {
            accountCollection.insertOne(new Document("userId", user.getIdAsString()).append("coin", 0));
            return new Account(user.getIdAsString());
        }

        return new Account(
                document.getString("userId")
        );
    }

    public static void giveCoin(Account account, int coin) {
        Document document = accountCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", account.userId()).append("coin", 0));
            document = accountCollection.find(new Document("userId", account.userId())).first();
        }

        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", new Document("coin", document.getInteger("coin") + coin)));
    }

    public static boolean isAttended(Account account) {
        LocalDate now = LocalDate.now();

        Document document = attendanceCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            attendanceCollection.insertOne(new Document("userId", account.userId()));
            return false;
        }

        return document.containsKey(now.toString()) && document.getBoolean(now.toString());
    }

    public static int attend(Account account) {
        LocalDate now = LocalDate.now();

        Document document = attendanceCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            attendanceCollection.insertOne(new Document("userId", account.userId()));
            document = attendanceCollection.find(new Document("userId", account.userId())).first();
        }

        document.append(now.toString(), true);
        attendanceCollection.updateOne(new Document("userId", account.userId()), new Document("$set", document));

        int ranking = 0;
        for (Document doc : attendanceCollection.find()) {
            if (doc.containsKey(now.toString()) && doc.getBoolean(now.toString())) ranking++;
        }

        return ranking;
    }

    public static int getCoin(String userId) {
        return getUserDocument(userId).getInteger("coin");
    }

    public static List<CoinRank> getCoinRanking() {
        List<CoinRank> ranking = new ArrayList<>();

        FindIterable<Document> documents = accountCollection.find().sort(new Document("coin", -1));
        for (Document document : documents) {
            int coin = document.getInteger("coin");
            ranking.add(new CoinRank(document.getString("userId"), coin));
        }
        return ranking;
    }
}
