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
        message.reply(
                new EmbedBuilder()
                .setTitle(user.getDisplayName(message.getServer().orElseThrow()) + "님의 지갑 :moneybag:")
                .addInlineField("보유 코인", Utils.displayCoin(account.coin()))
        );
    }
}
