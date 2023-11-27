package me.syuk.saenggang.commands;

import me.syuk.saenggang.Account;
import org.javacord.api.entity.message.Message;

public class PointCommand implements Command{
    @Override
    public String name() {
        return "포인트";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        message.reply("지금 당신의 포인트는 `\uD83E\uDE99" + account.point() + "`입니다!");
    }
}
