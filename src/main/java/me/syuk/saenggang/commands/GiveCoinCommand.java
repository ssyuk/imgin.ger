package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

public class GiveCoinCommand implements Command {
    @Override
    public String name() {
        return "지급";
    }

    @Override
    public Theme theme() {
        return Theme.FOR_OWNER;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (message.getAuthor().isBotOwner()) {
            if (args.length != 3) {
                message.reply("지급 명령어는 `지급 [@유저] [코인]` 형식으로 사용해주세요!");
                return;
            }
            String userId = args[1].replace("<@", "").replace(">", "");
            int count = Integer.parseInt(args[2]);
            DBManager.Account target = new DBManager.Account(userId);
            target.giveCoin(message.getChannel(), count);
        } else {
            message.reply("이 명령어는 봇 주인만 사용할 수 있어요!");
        }
    }
}
