package me.syuk.saenggang.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import me.syuk.saenggang.Utils;
import org.bson.Document;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.syuk.saenggang.Main.properties;

public class DBManager {
    private static MongoCollection<Document> messageCollection;
    private static MongoCollection<Document> accountCollection;

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
    }

    public static List<SaenggangKnowledge> getKnowledge(String command) {
        FindIterable<Document> documents = messageCollection.find(new Document("question", command));

        List<SaenggangKnowledge> knowledge = new ArrayList<>();
        for (Document document : documents) {
            knowledge.add(new SaenggangKnowledge(
                    document.getString("question"),
                    document.getString("answer"),
                    document.getString("authorName"),
                    document.getString("authorId")
            ));
        }
        return knowledge;
    }

    public static void addKnowledge(SaenggangKnowledge message) {
        Document document = new Document("question", message.question())
                .append("answer", message.answer())
                .append("authorName", message.authorName())
                .append("authorId", message.authorId());

        messageCollection.insertOne(document);
    }

    public static void removeKnowledge(SaenggangKnowledge message) {
        Document document = new Document("question", message.question())
                .append("answer", message.answer())
                .append("authorName", message.authorName())
                .append("authorId", message.authorId());

        messageCollection.deleteMany(document);
    }

    public static Map<String, List<SaenggangKnowledge>> getKnowledgeList() {
        FindIterable<Document> documents = messageCollection.find();

        Map<String, List<SaenggangKnowledge>> knowledgeList = new HashMap<>();
        for (Document document : documents) {
            List<SaenggangKnowledge> knowledge = new ArrayList<>();
            String question = document.getString("question");
            String answer = document.getString("answer");
            String authorName = document.getString("authorName");
            String authorId = document.getString("authorId");

            SaenggangKnowledge message = new SaenggangKnowledge(question, answer, authorName, authorId);
            knowledge.add(message);

            knowledgeList.put(question, knowledge);
        }
        return knowledgeList;
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
        return new Account(getUserDocument(user.getIdAsString()).getString("userId"));
    }

    public static void giveCoin(Account account, int coin) {
        Document document = accountCollection.find(new Document("userId", account.userId())).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", account.userId()).append("coin", 0));
            document = accountCollection.find(new Document("userId", account.userId())).first();
            assert document != null;
        }

        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", new Document("coin", document.getInteger("coin") + coin)));
    }

    public static boolean isAttended(Account account) {
        LocalDate now = LocalDate.now();
        Document document = getUserDocument(account.userId());
        return document.containsKey("latestAttendance") && document.getString("latestAttendance").equals(now.toString());
    }

    public static AttendStatus attend(Account account) {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        Document document = getUserDocument(account.userId());
        if (isAttended(account)) return new AttendStatus(0, 0);

        if (document.containsKey("latestAttendance") && document.getString("latestAttendance").equals(yesterday.toString())) {
            document.put("attendanceStreak", document.getInteger("attendanceStreak", 0) + 1);
        } else {
            document.put("attendanceStreak", 1);
        }
        document.put("latestAttendance", now.toString());
        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", document));

        int ranking = 0;
        for (Document doc : accountCollection.find()) {
            if (doc.containsKey("latestAttendance") && doc.getString("latestAttendance").equals(now.toString()))
                ranking++;
        }

        return new AttendStatus(ranking, document.getInteger("attendanceStreak", 1));
    }

    public static int getCoin(String userId) {
        return getUserDocument(userId).getInteger("coin");
    }

    public static List<CoinRank> getCoinRanking() {
        List<CoinRank> ranking = new ArrayList<>();

        FindIterable<Document> documents = accountCollection.find().sort(new Document("coin", -1));
        for (Document document : documents) {
            int coin = document.getInteger("coin");
            if (coin != 0) ranking.add(new CoinRank(document.getString("userId"), coin));
        }
        return ranking;
    }

    public record AttendStatus(int ranking, int streak) {
    }

    public record CoinRank(String user, int coin) {
    }

    public record Account(String userId) {
        public int coin() {
            return getCoin(userId);
        }

        public void giveCoin(TextChannel channel, int count, String reason) {
            if (!reason.isEmpty()) reason += " ";
            if (count > 0) {
                DBManager.giveCoin(this, count);
                channel.sendMessage("<@" + userId + ">님! " + reason + Utils.displayCoin(count) + "을(를) 받았어요. (현재 코인: " + Utils.displayCoin(coin()) + ")");
            } else if (count < 0) {
                DBManager.giveCoin(this, count);
                channel.sendMessage("<@" + userId + ">님! " + reason + Utils.displayCoin(-count) + "을(를) 잃었어요. (현재 코인: " + Utils.displayCoin(coin()) + ")");
            }
        }

        public void giveCoin(TextChannel channel, int count) {
            giveCoin(channel, count, "");
        }
    }

    public record SaenggangKnowledge(String question, String answer, String authorName, String authorId) {
    }
}
