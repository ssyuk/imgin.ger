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
        return Utils.getRankingEmoji(getRank(DBManager.getAccount(user))) + user.getName();
    }

    public static String getRankingEmoji(int rank, String defaultEmoji) {
        return switch (rank) {
            case 1 -> "\uD83E\uDD47 ";
            case 2 -> "\uD83E\uDD48 ";
            case 3 -> "\uD83E\uDD49 ";
            default -> defaultEmoji;
        };
    }

    public static String getRankingEmoji(int rank) {
        return getRankingEmoji(rank, "");
    }
}
