package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.concurrent.ExecutionException;

public class SendCoinCommand implements Command {
    @Override
    public String name() {
        return "선물";
    }

    @Override
    public Theme theme() {
        return Theme.ACCOUNT;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (args.length < 2) {
            message.reply("사용법: 생강아 선물 <유저태그> <코인>");
            return;
        }

        String targetTag = args[1];
        int coin = Integer.parseInt(args[2]);

        String userId = targetTag.substring(2, targetTag.length() - 1);
        DBManager.Account target;
        try {
            target = DBManager.getAccount(message.getApi().getUserById(userId).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (account.coin() < coin) {
            message.reply("코인이 부족합니다.");
            return;
        }

        account.giveCoin(message.getChannel(), -coin, targetTag + "님에게 선물하는데");
        target.giveCoin(message.getChannel(), coin, "<@" + account.userId() + ">님이 선물해주셔서");
        message.reply(targetTag + "님에게 " + coin + "코인을 선물했습니다.");
    }
}
