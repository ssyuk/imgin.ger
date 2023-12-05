package me.syuk.saenggang.commands.account;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import static me.syuk.saenggang.Main.api;

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
    public void execute(DBManager.Account sender, String[] args, Message message) {
        User user = message.getUserAuthor().orElseThrow();
        DBManager.Account account = sender;
        if (args.length > 1) {
            String userId = args[1].replace("<@", "").replace(">", "");
            user = api.getUserById(userId).join();
            account = DBManager.getAccount(user);
        }

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
