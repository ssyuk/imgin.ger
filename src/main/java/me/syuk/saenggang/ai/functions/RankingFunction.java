package me.syuk.saenggang.ai.functions;

import com.google.gson.JsonObject;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class RankingFunction implements AIFunction {
    @Override
    public String name() {
        return "ranking";
    }

    @Override
    public String description() {
        return "전체 사용자중, 1위부터 7위까지의 코인 랭킹을 보여줍니다.";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public boolean isTalkingFunction() {
        return false;
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        List<DBManager.CoinRank> ranking = DBManager.getCoinRanking();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(ranking.size(), 7); i++) {
            DBManager.CoinRank rank = ranking.get(i);
            builder.append("**").append(Utils.getRankBadge(i + 1)).append((i + 1)).append("위** <@").append(rank.userId()).append("> ").append(Utils.displayCoin(rank.coin())).append("\n");
        }

        if (ranking.size() > 7 && ranking.subList(0, 7).stream().noneMatch(rank -> rank.userId() == account.userId())) {
            int myRank;
            DBManager.CoinRank coinRank;
            for (myRank = 0; myRank < ranking.size(); myRank++)
                if (ranking.get(myRank).userId() == account.userId()) break;
            if (myRank < ranking.size()) {
                coinRank = ranking.get(myRank);
                builder.append("...").append("\n");
                builder.append("**").append(myRank + 1).append("위** <@").append(coinRank.userId()).append("> ").append(Utils.displayCoin(coinRank.coin())).append("\n");
                builder.append("...").append("\n");
            }
        }

        if (ranking.size() > 7) {
            int count = ranking.size() - 7;
            if (ranking.subList(0, 7).stream().noneMatch(rank -> rank.userId() == account.userId())) count--;
            builder.append("외 ").append(count).append("명");
        }

        requestMessage.reply(new EmbedBuilder()
                .setTitle("코인 랭킹 (전체서버)")
                .setDescription(builder.toString())
                .setColor(Color.green)
                .setFooter("코인은 게임, 출석체크 등으로 획득할 수 있습니다.")
        );

        return new JsonObject();
    }
}
