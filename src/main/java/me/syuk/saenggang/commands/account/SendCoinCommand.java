package me.syuk.saenggang.commands.account;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

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
        try {
            long userId = Long.parseLong(args[1].replace("<@", "").replace(">", ""));
            DBManager.Account target = new DBManager.Account(userId);
            int count = Integer.parseInt(args[2]);
            if (count <= 0) {
                message.reply("선물할 코인은 양수(+)여야해요.");
                return;
            } else if (count > 150) {
                message.reply("**1회** 선물 한도는 " + Utils.displayCoin(150) + "이에요.");
                return;
            } else if (count + account.sentCoin() > 300) {
                message.reply("**1일** 선물 한도는 " + Utils.displayCoin(300) + "이에요.");
                return;
            } else if (account.coin() < count) {
                message.reply("코인이 부족해요.");
                return;
            } else if (account.userId() == target.userId()) {
                message.reply("자기 자신에게는 선물할 수 없어요.");
                return;
            } else if (Utils.isBot(target.userId())) {
                message.reply("봇에게는 선물할 수 없어요.");
                return;
            }

            account.addSentCoin(count);
            account.giveCoin(message.getChannel(), -count, "<@" + target.userId() + ">님께 선물하는데");
            target.giveCoin(message.getChannel(), count, "<@" + account.userId() + ">님이 선물해주셔서");
            message.reply("<@" + target.userId() + ">님께 " + Utils.displayCoin(count) + "을 선물하셨어요! (오늘 한도: " + Utils.displayCoin(account.sentCoin()) + "/" + Utils.displayCoin(300) + ")");
        } catch (Exception e) {
            message.reply("선물 명령어는 `선물 [@유저] [코인]` 형식으로 사용해주세요!");
        }
    }
}
