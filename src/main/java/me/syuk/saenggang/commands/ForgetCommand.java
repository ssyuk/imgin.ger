package me.syuk.saenggang.commands;

import me.syuk.saenggang.DBManager;
import me.syuk.saenggang.SaenggangKnown;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

public class ForgetCommand implements Command {
    @Override
    public String name() {
        return "잊어";
    }

    @Override
    public void execute(User user, String[] args, Message message) {
        if (args.length != 2) {
            message.reply("잊어 명령어는 `잊어 [명령어]` 형식으로 사용해주세요!");
            return;
        }
        String question = args[1];
        SaenggangKnown known = DBManager.getKnown(question);
        if (known == null) {
            message.reply("그런 명령어는 없어요!");
            return;
        }
        if (!known.authorId().equals(user.getIdAsString())) {
            message.reply("알려주신 분만 잊게할 수 있어요!");
            return;
        }
        DBManager.removeKnown(known);
        message.reply("`" + question + "`이 뭐였죠?");
    }
}
