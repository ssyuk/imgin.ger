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
import java.time.LocalDateTime;
import java.util.*;

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
                    document.getLong("authorId")
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
            long authorId = document.getLong("authorId");

            SaenggangKnowledge message = new SaenggangKnowledge(question, answer, authorName, authorId);
            knowledge.add(message);

            knowledgeList.put(question, knowledge);
        }
        return knowledgeList;
    }

    public static Document getUserDocument(long userId) {
        Document document = accountCollection.find(new Document("userId", userId)).first();
        if (document == null) {
            accountCollection.insertOne(new Document("userId", userId).append("coin", 0));
            document = accountCollection.find(new Document("userId", userId)).first();
        }
        assert document != null;
        if (!document.containsKey("coinHistory")) {
            int currentCoin = document.getInteger("coin");
            accountCollection.updateOne(new Document("userId", userId),
                    new Document("$set", new Document("coinHistory", new ArrayList<>(List.of(
                            new Document("coin", currentCoin).append("date", LocalDateTime.now().toString())
                    )))));
        }
        return document;
    }

    public static Account getAccount(User user) {
        return new Account(getUserDocument(user.getId()).getLong("userId"));
    }

    public static void giveCoin(Account account, int coin) {
        Document document = getUserDocument(account.userId);

        List<Document> coinHistory = document.getList("coinHistory", Document.class, new ArrayList<>());
        coinHistory.add(new Document("coin", coin).append("date", LocalDateTime.now().toString()));

        accountCollection.updateOne(new Document("userId", account.userId()),
                new Document("$set",
                        new Document("coin", document.getInteger("coin") + coin)
                                .append("coinHistory", coinHistory)
                ));
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

        return new AttendStatus(ranking, getAttendanceStreak(account));
    }

    public static int getAttendanceStreak(Account account) {
        return getUserDocument(account.userId()).getInteger("attendanceStreak", 0);
    }

    public static int getCoin(long userId) {
        return getUserDocument(userId).getInteger("coin");
    }

    public static List<CoinRank> getCoinRanking() {
        List<CoinRank> ranking = new ArrayList<>();

        FindIterable<Document> documents = accountCollection.find().sort(new Document("coin", -1));
        for (Document document : documents) {
            int coin = document.getInteger("coin");
            if (coin != 0) ranking.add(new CoinRank(document.getLong("userId"), coin));
        }
        return ranking;
    }

    public static List<CoinHistory> getCoinHistory(Account account) {
        List<CoinHistory> coinHistory = new ArrayList<>();

        Document document = getUserDocument(account.userId);
        List<Document> history = document.getList("coinHistory", Document.class, new ArrayList<>());
        for (Document doc : history) {
            coinHistory.add(new CoinHistory(doc.getInteger("coin")));
        }
        return coinHistory;
    }

    public static int getUserBadgeId(Account account) {
        return getUserDocument(account.userId).getInteger("currentBadge", 0);
    }

    public static boolean addBadge(Account account, int badgeId) {
        Document document = getUserDocument(account.userId);
        List<Integer> badges = document.getList("badges", Integer.class, new ArrayList<>());
        if (badges.contains(badgeId)) return false;
        badges.add(badgeId);
        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", new Document("badges", badges)));
        return true;
    }

    public static List<Integer> getBadges(Account account) {
        return getUserDocument(account.userId).getList("badges", Integer.class, new ArrayList<>());
    }

    public static void selectBadge(Account account, int badge) {
        if (badge < 1 || badge > Utils.LAST_BADGE) return;
        if (!getBadges(account).contains(badge)) return;

        accountCollection.updateOne(new Document("userId", account.userId()), new Document("$set", new Document("currentBadge", badge)));
    }

    public record AttendStatus(int ranking, int streak) {
    }

    public record CoinRank(long userId, int coin) {
    }

    public record Account(long userId) {
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
                channel.sendMessage("<@" + userId + ">님! " + reason + Utils.displayCoin(-count) + "을(를) 사용했어요. (현재 코인: " + Utils.displayCoin(coin()) + ")");
            }
        }

        public void giveCoin(TextChannel channel, int count) {
            giveCoin(channel, count, "");
        }

        public int sentCoin() {
            if (!Objects.equals(getUserDocument(userId).getString("sentDate"), LocalDate.now().toString())) {
                accountCollection.updateOne(new Document("userId", userId), new Document("$set",
                        new Document("sentCoin", 0).append("sentDate", LocalDate.now().toString())
                ));
                return 0;
            }

            return getUserDocument(userId).getInteger("sentCoin", 0);
        }

        public void addSentCoin(int count) {
            if (!Objects.equals(getUserDocument(userId).getString("sentDate"), LocalDate.now().toString())) {
                accountCollection.updateOne(new Document("userId", userId), new Document("$set",
                        new Document("sentCoin", 0).append("sentDate", LocalDate.now().toString())
                ));
            }

            accountCollection.updateOne(new Document("userId", userId), new Document("$set",
                    new Document("sentCoin", sentCoin() + count).append("sentDate", LocalDate.now().toString())
            ));
        }
    }

    public record SaenggangKnowledge(String question, String answer, String authorName, long authorId) {
    }

    public record CoinHistory(int coin) {
    }
}
