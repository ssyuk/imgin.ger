package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class RankingCommand implements Command {
    @Override
    public String name() {
        return "랭킹";
    }

    @Override
    public Theme theme() {
        return Theme.ACCOUNT;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        List<DBManager.CoinRank> ranking = DBManager.getCoinRanking();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(ranking.size(), 7); i++) {
            DBManager.CoinRank rank = ranking.get(i);

            builder.append("**").append(Utils.getRankingEmoji(i + 1)).append((i + 1)).append("위** <@").append(rank.user()).append("> ").append(Utils.displayCoin(rank.coin())).append("\n");
        }

        if (ranking.size() > 7 && ranking.subList(0, 7).stream().noneMatch(rank -> rank.user().equals(account.userId()))) {
            int myRank;
            DBManager.CoinRank coinRank;
            for (myRank = 0; myRank < ranking.size(); myRank++)
                if (ranking.get(myRank).user().equals(account.userId())) break;
            coinRank = ranking.get(myRank);
            builder.append("...").append("\n");
            builder.append("**").append(myRank + 1).append("위** <@").append(coinRank.user()).append("> ").append(Utils.displayCoin(coinRank.coin())).append("\n");
            builder.append("...").append("\n");
        }

        if (ranking.size() > 7) {
            int count = ranking.size() - 7;
            if (ranking.subList(0, 7).stream().noneMatch(rank -> rank.user().equals(account.userId()))) count--;
            builder.append("외 ").append(count).append("명");
        }

        message.reply(new EmbedBuilder()
                .setTitle("코인 랭킹 (전체서버)")
                .setDescription(builder.toString())
                .setColor(Color.green)
                .setFooter("코인은 게임, 출석체크 등으로 획득할 수 있습니다.")
        );
    }
}
