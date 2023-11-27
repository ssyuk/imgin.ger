package me.syuk.saenggang.commands;

import me.syuk.saenggang.Account;
import me.syuk.saenggang.DBManager;
import org.javacord.api.entity.message.Message;

public class AttendanceCommand implements Command{
    @Override
    public String name() {
        return "출첵";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        if (DBManager.isAttended(account)) {
            message.reply("이미 출석했어요!");
            return;
        }

        DBManager.attend(account);
        DBManager.givePoint(account, 5);
        message.reply("출석했어요!\n`\uD83E\uDE995`을(를) 받았어요!");
    }
}
