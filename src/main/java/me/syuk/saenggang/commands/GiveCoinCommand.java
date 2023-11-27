package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.message.Message;

import static me.syuk.saenggang.Main.api;

public class GiveCoinCommand implements Command {
    @Override
    public String name() {
        return "지급";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        if (api.getUserById(account.userId()).join().isBotOwner()) {
            if (args.length != 3) {
                message.reply("지급 명령어는 `지급 [유저] [코인]` 형식으로 사용해주세요!");
                return;
            }
            String userId = args[1];
            int count = Integer.parseInt(args[2]);
            Account target = new Account(userId);
            target.giveCoin(message.getChannel(), count);
        } else {
            message.reply("이 명령어는 봇 주인만 사용할 수 있어요!");
        }
    }
}
