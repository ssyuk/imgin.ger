package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

public class WalletCommand implements Command {
    @Override
    public String name() {
        return "지갑";
    }

    @Override
    public Theme theme() {
        return Theme.ACCOUNT;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        User user = message.getUserAuthor().orElseThrow();
        int rank = Utils.getRank(account);
        message.reply(
                new EmbedBuilder()
                        .setTitle(Utils.getUserName(user) + "님의 지갑")
                        .addInlineField("보유 코인", Utils.displayCoin(account.coin()))
                        .addInlineField("코인 랭킹", Utils.getRankBadge(rank) + rank + "위")
                        .addInlineField("연속 출석", DBManager.getAttendanceStreak(account) + "회")
        );
    }
}
