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
        if (account.coin() < 100) {
            message.reply("코인이 부족합니다.");
            return;
        }

        int badgeId = DBManager.drawBadge(account);
        account.giveCoin(message.getChannel(), -100, "뱃지뽑기에");
        if (DBManager.addBadge(account, badgeId)) {
            message.reply("뽑은 뱃지: " + Utils.getBadge(badgeId)).whenComplete((message1, throwable) -> {
                if (throwable != null) throwable.printStackTrace();
            });
        } else {
            message.reply("뽑은 뱃지: " + Utils.getBadge(badgeId) + "\n이미 가지고 계신 뱃지라서 " + Utils.displayCoin(50) + "을 돌려드렸어요!").whenComplete((message1, throwable) -> {
                if (throwable != null) throwable.printStackTrace();
            });
            DBManager.giveCoin(account, 50);
        }
    }
}
