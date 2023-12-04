package me.syuk.saenggang;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.util.List;

public class Utils {
    public static ServerThreadChannel createGameThread(Message message, String name) {
        return message.createThread(name + " with " + message.getAuthor().getName(), AutoArchiveDuration.ONE_HOUR).join();
    }

    public static String displayCoin(int coin) {
        return "`\uD83E\uDE99" + coin + "`";
    }

    public static int getRank(DBManager.Account account) {
        List<DBManager.CoinRank> ranking = DBManager.getCoinRanking();
        int myRank = 0;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).user().equals(account.userId())) {
                myRank = i;
                break;
            }
        }
        return myRank + 1;
    }

    public static String getUserName(User user) {
        DBManager.Account account = DBManager.getAccount(user);
        return getRankBadge(getRank(account)) + getBadge(DBManager.getUserBadgeId(account)) + user.getName();
    }

    public static String getRankBadge(int rank) {
        return switch (rank) {
            case 1 -> "\uD83E\uDD47 ";
            case 2 -> "\uD83E\uDD48 ";
            case 3 -> "\uD83E\uDD49 ";
            default -> "";
        };
    }

    public static String getBadge(int badgeId) {
        String badgeEmoji = switch (badgeId) {
            case 1 -> ":poop:";
            case 2 -> ":dog:";
            case 3 -> ":cat:";
            case 4 -> ":mouse:";
            case 5 -> ":hamster:";
            case 6 -> ":fox:";
            case 7 -> ":bear:";
            case 8 -> ":panda_face:";
            case 9 -> ":koala:";
            case 10 -> ":tiger:";
            case 11 -> ":lion:";
            case 12 -> ":cow:";
            case 13 -> ":pig:";
            case 14 -> ":frog:";
            case 15 -> ":monkey_face:";
            case 16 -> ":chicken:";
            case 17 -> ":penguin:";
            case 18 -> ":bird:";
            case 19 -> ":baby_chick:";
            case 20 -> ":hatching_chick:";
            case 21 -> ":hatched_chick:";
            case 22 -> ":wolf:";
            case 23 -> ":boar:";
            default -> "";
        };
        if (!badgeEmoji.isEmpty()) badgeEmoji = badgeEmoji + " ";
        return badgeEmoji;
    }
}
