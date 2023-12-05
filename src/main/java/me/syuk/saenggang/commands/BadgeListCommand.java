package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

public class BadgeListCommand implements Command {
    @Override
    public String name() {
        return "뱃지도감";
    }

    @Override
    public Theme theme() {
        return Theme.COSMETIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= Utils.LAST_BADGE; i++) {
            builder.append(Utils.getBadge(i));
        }
        message.reply(builder.toString()).whenComplete((message1, throwable) -> {
            if (throwable != null) throwable.printStackTrace();
        });
    }
}
