package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

public class AttendanceCommand implements Command {
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

        int ranking = DBManager.attend(account);
        message.reply("\uD83D\uDDF3️ " + ranking + "등으로 출석했어요!");
        if (ranking == 1) account.giveCoin(message.getChannel(), 8, "출석 1등 보상으로");
        else if (ranking == 2) account.giveCoin(message.getChannel(), 5, "출석 2등 보상으로");
        else if (ranking == 3) account.giveCoin(message.getChannel(), 3, "출석 3등 보상으로");
        else account.giveCoin(message.getChannel(), 1, "출석 보상으로");
    }
}