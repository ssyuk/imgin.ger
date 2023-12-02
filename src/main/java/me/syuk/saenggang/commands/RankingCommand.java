package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.db.CoinRank;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.List;

public class RankingCommand implements Command {
    @Override
    public String name() {
        return "랭킹";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        List<CoinRank> ranking = DBManager.getCoinRanking();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("코인 랭킹 (전체서버)");

        for (int i = 0; i < Math.min(ranking.size(), 10); i++) {
            CoinRank rank = ranking.get(i);
            if (rank.coin() == 0) break;

            String emoji = switch (i) {
                case 0 -> "\uD83E\uDD47 ";
                case 1 -> "\uD83E\uDD48 ";
                case 2 -> "\uD83E\uDD49 ";
                default -> "";
            };
            builder.addField(emoji + (i + 1) + "위", "<@" + rank.user() + "> " + Utils.displayCoin(rank.coin()), false);
        }

        message.reply(builder);
    }
}
