package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.db.SaenggangKnowledge;
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

            DBManager.addKnowledge(new SaenggangKnowledge(question, answer, authorName, authorId));
            message.reply("알겠습니다! `" + question + "`은 `" + answer + "`라고 배웠어요.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
