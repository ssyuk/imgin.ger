package me.syuk.saenggang;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.util.List;

import static me.syuk.saenggang.Main.api;

public class Utils {
    public static final int LAST_BADGE = 23;

    public static ServerThreadChannel createGameThread(Message message, String name) {
        return message.createThread(name + " with " + message.getAuthor().getName(), AutoArchiveDuration.ONE_HOUR).join();
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String displayCoin(int coin) {
        return "`\uD83E\uDE99" + coin + "`";
    }

    public static int getRank(DBManager.Account account) {
        List<DBManager.CoinRank> ranking = DBManager.getCoinRanking();
        int myRank = -1;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).userId() == account.userId()) {
                myRank = i;
                break;
            }
        }
        return myRank + 1;
    }

    public static String getUserName(User user) {
        DBManager.Account account = DBManager.getAccount(user);
        StringBuilder builder = new StringBuilder();
        builder.append(getRankBadge(getRank(account)));
        Badge badge = Badge.getBadgeById(DBManager.getUserBadgeId(account));
        if (badge != null) {
            builder.append(badge.getEmoji()).append(" ");
        }
        return builder + user.getName();
    }

    public static String getRankBadge(int rank) {
        return switch (rank) {
            case 1 -> "\uD83E\uDD47 ";
            case 2 -> "\uD83E\uDD48 ";
            case 3 -> "\uD83E\uDD49 ";
            default -> "";
        };
    }

    public static boolean isBot(long id) {
        return isBot(api.getUserById(id).join());
    }

    public static boolean isBot(User user) {
        return user.isBot() && user.getId() != 1023553267759845527L;
    }
}
