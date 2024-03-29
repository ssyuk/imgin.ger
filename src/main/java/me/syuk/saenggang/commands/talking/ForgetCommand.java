package me.syuk.saenggang.commands.talking;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
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
    public Theme theme() {
        return Theme.TALKING;
    }
    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (args.length != 2) {
            message.reply("잊어 명령어는 `잊어 [명령어]` 형식으로 사용해주세요!");
            return;
        }
        String question = args[1];
        List<DBManager.SaenggangKnowledge> knowledge = DBManager.getKnowledge(question);

        if (knowledge.isEmpty()) {
            message.reply("그런 명령어는 없어요!");
            return;
        }

        List<DBManager.SaenggangKnowledge> knowledgeByUser = new ArrayList<>();
        if (message.getAuthor().isBotOwner()) knowledgeByUser.addAll(knowledge);
        else for (DBManager.SaenggangKnowledge known : knowledge)
            if (known.authorId() == account.userId()) knowledgeByUser.add(known);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("어떤걸 잊게 하실 건가요?")
                .setDescription("이곳에 잊게할 명령어의 번호를 입력해주세요!");

        embed.addField("0번", "*취소*");
        for (int i = 0; i < knowledgeByUser.size(); i++) {
            DBManager.SaenggangKnowledge known = knowledgeByUser.get(i);
            embed.addField(i + 1 + "번" + (known.authorId() != account.userId() ? "*" : ""), MessageCreated.fixAnswer(known.answer(), account));
        }
        message.reply(embed);

        MessageCreated.replyCallbackMap.put(account, (msg) -> {
            String content = msg.getContent();
            if (content.equals("전체") && message.getAuthor().isBotOwner()) {
                for (DBManager.SaenggangKnowledge known : knowledge) DBManager.removeKnowledge(known);
                msg.reply("알겠습니다! `" + question + "`을(를) 전부 잊었어요.");
                MessageCreated.replyCallbackMap.remove(account);
                return true;
            }

            try {
                int index = Integer.parseInt(content) - 1;
                if (index == -1) {
                    msg.reply("취소되었어요!");
                    MessageCreated.replyCallbackMap.remove(account);
                    return true;
                }
                DBManager.SaenggangKnowledge known = knowledgeByUser.get(index);
                DBManager.removeKnowledge(known);
                msg.reply("알겠습니다! `" + known.question() + "`을(를) 잊었어요.");
                MessageCreated.replyCallbackMap.remove(account);
            } catch (NumberFormatException e) {
                msg.reply("숫자만 입력해주세요!");
            } catch (IndexOutOfBoundsException e) {
                msg.reply("잘못된 번호에요!");
            }
            return true;
        });
    }
}
