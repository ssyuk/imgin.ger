package me.syuk.saenggang.commands;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.db.SaenggangKnowledge;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

public class ForgetCommand implements Command {
    @Override
    public String name() {
        return "잊어";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        if (args.length != 2) {
            message.reply("잊어 명령어는 `잊어 [명령어]` 형식으로 사용해주세요!");
            return;
        }
        String question = args[1];
        List<SaenggangKnowledge> knowledge = DBManager.getKnowledge(question);

        if (knowledge.isEmpty()) {
            message.reply("그런 명령어는 없어요!");
            return;
        }

        List<SaenggangKnowledge> knowledgeByUser = new ArrayList<>();
        for (SaenggangKnowledge known : knowledge)
            if (known.authorId().equals(account.userId())) knowledgeByUser.add(known);

        if (knowledgeByUser.isEmpty()) {
            message.reply("알려주신 분만 잊게할 수 있어요!");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("어떤걸 잊게 하실 건가요?")
                .setDescription("이곳에 잊게할 명령어의 번호를 입력해주세요!");

        for (int i = 0; i < knowledgeByUser.size(); i++) {
            SaenggangKnowledge known = knowledgeByUser.get(i);
            embed.addField(i + 1 + "번", known.answer());
        }
        message.reply(embed);

        MessageCreated.replyListener.put(account, (msg) -> {
            try {
                int index = Integer.parseInt(msg.getContent()) - 1;
                SaenggangKnowledge known = knowledgeByUser.get(index);
                DBManager.removeKnowledge(known);
                msg.reply("알겠습니다! `" + known.question() + "`을(를) 잊었어요.");
                MessageCreated.replyListener.remove(account);
            } catch (NumberFormatException e) {
                msg.reply("숫자만 입력해주세요!");
            } catch (IndexOutOfBoundsException e) {
                msg.reply("잘못된 번호에요!");
            }
        });
    }
}
