package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.db.SaenggangKnowledge;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.concurrent.ExecutionException;

import static me.syuk.saenggang.Main.api;

public class LearnCommand implements Command {
    @Override
    public String name() {
        return "배워";
    }

    @Override
    public Theme theme() {
        return Theme.TALKING;
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        try {
            if (args.length != 3) {
                message.reply(new EmbedBuilder()
                        .setTitle("배워 명령어는 `배워 [명령어] [메시지]` 형식으로 사용해주세요!")
                        .setColor(Color.red)
                        .setDescription("사용 가능한 파라미터:")
                        .addInlineField("{user.name}", "사용자를 멘션(@)합니다.")
                        .addInlineField("{user.coin}", "사용자의 코인을 표시합니다.")
                        .addInlineField("{user.displayCoin}", "사용자의 코인을 표시합니다. (코인 이모지와 함께)")
                );
                return;
            }
            String question = args[1];
            String answer = args[2];
            String authorName = api.getUserById(account.userId()).get().getName();
            String authorId = account.userId();

            if (DBManager.getKnowledge(question).stream().map(k -> k.question() + k.answer()).anyMatch(s -> s.equals(question + answer))) {
                message.reply("같은 질문, 같은 답변으로는 배울 수 없어요!");
                return;
            }

            DBManager.addKnowledge(new SaenggangKnowledge(question, answer, authorName, authorId));
            message.reply("알겠습니다! `" + question + "`은 `" + answer + "`라고 배웠어요.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
