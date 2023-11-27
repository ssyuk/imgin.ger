package me.syuk.saenggang.commands;

import me.syuk.saenggang.DBManager;
import me.syuk.saenggang.SaenggangKnown;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

public class LearnCommand implements Command{
    @Override
    public String name() {
        return "배워";
    }

    @Override
    public void execute(User user, String[] args, Message message) {
        if (args.length != 3) {
            message.reply("배워 명령어는 `배워 [명령어] [메시지]` 형식으로 사용해주세요!");
            return;
        }
        String question = args[1];
        String answer = args[2];
        String authorName = user.getName();
        String authorId = user.getIdAsString();

        if (DBManager.getKnown(question) != null) {
            message.reply("이미 알고있어요..");
            return;
        }

        DBManager.addKnown(new SaenggangKnown(question, answer, authorName, authorId));
        message.reply("알겟ㅅ슴미다ㅋ `" + question + "`은 `" + answer + "`라거하라구배웟떠요");
    }
}
