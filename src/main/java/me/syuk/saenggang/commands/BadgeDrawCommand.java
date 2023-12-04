package me.syuk.saenggang.commands;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

public class BadgeDrawCommand implements Command {
    @Override
    public String name() {
        return "뱃지뽑기";
    }

    @Override
    public Theme theme() {
        return Theme.COSMETIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (account.coin() < 300) {
            message.reply("코인이 부족합니다.");
            return;
        }

        int badgeId = DBManager.drawBadge(account);
        message.reply("뽑은 뱃지: " + Utils.getBadge(badgeId));
        account.giveCoin(message.getChannel(), -300, "뱃지뽑기에");
        DBManager.addBadge(account, badgeId);
    }
}
