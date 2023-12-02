package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.CoinRank;
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
    public void execute(Account account, String[] args, Message message) {
        List<CoinRank> ranking = DBManager.getCoinRanking();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(ranking.size(), 7); i++) {
            CoinRank rank = ranking.get(i);

            String emoji = switch (i) {
                case 0 -> "\uD83E\uDD47 ";
                case 1 -> "\uD83E\uDD48 ";
                case 2 -> "\uD83E\uDD49 ";
                default -> "";
            };
            builder.append("**").append(emoji).append((i + 1)).append("위** <@").append(rank.user()).append("> ").append(Utils.displayCoin(rank.coin())).append("\n");
        }

        if (ranking.size() > 7 && ranking.subList(0, 7).stream().noneMatch(rank -> rank.user().equals(account.userId()))) {
            int myRank;
            CoinRank coinRank;
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
                .setFooter("코인은 끝말잇기, 출석체크 등으로 획득할 수 있습니다.")
        );
    }
}
