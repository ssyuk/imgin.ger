package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.message.Message;

public class CoinCommand implements Command{
    @Override
    public String name() {
        return "코인";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        message.reply("지금 당신의 코인은 `\uD83E\uDE99" + account.coin() + "`입니다!");
    }
}
