package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

public class AttendanceCommand implements Command {
    @Override
    public String name() {
        return "출첵";
    }

    @Override
    public Theme theme() {
        return Theme.ACCOUNT;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (DBManager.isAttended(account)) {
            message.reply("이미 출석했어요!");
            return;
        }

        DBManager.AttendStatus status = DBManager.attend(account);
        message.reply("\uD83D\uDDF3️ " + status.ranking() + "등으로 출석했어요! (현재 연속출석 " + status.streak() + "일)");
        if (status.ranking() == 1) account.giveCoin(message.getChannel(), 15, "출석 1등 보상으로");
        else if (status.ranking() == 2) account.giveCoin(message.getChannel(), 10, "출석 2등 보상으로");
        else if (status.ranking() == 3) account.giveCoin(message.getChannel(), 7, "출석 3등 보상으로");
        else account.giveCoin(message.getChannel(), 5, "출석 보상으로");
    }
}
