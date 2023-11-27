package me.syuk.saenggang.commands;

import me.syuk.saenggang.Account;
import me.syuk.saenggang.DBManager;
import me.syuk.saenggang.SaenggangKnown;
import org.javacord.api.entity.message.Message;

import java.util.concurrent.ExecutionException;

import static me.syuk.saenggang.Main.api;

public class LearnCommand implements Command{
    @Override
    public String name() {
        return "배워";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        try {
            if (args.length != 3) {
                message.reply("배워 명령어는 `배워 [명령어] [메시지]` 형식으로 사용해주세요!");
                return;
            }
            String question = args[1];
            String answer = args[2];
            String authorName = api.getUserById(account.userId()).get().getName();
            String authorId = account.userId();

            if (DBManager.getKnown(question) != null) {
                message.reply("이미 알고있어요..");
                return;
            }

            DBManager.addKnown(new SaenggangKnown(question, answer, authorName, authorId));
            message.reply("알겟ㅅ슴미다ㅋ `" + question + "`은 `" + answer + "`라거하라구배웟떠요");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
