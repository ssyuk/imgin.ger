package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.List;

public class KnowledgeCommand implements Command {
    @Override
    public String name() {
        return "지식";
    }

    @Override
    public Theme theme() {
        return Theme.TALKING;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (args.length != 2) {
            message.reply("지식 명령어는 `지식 [명령어]` 형식으로 사용해주세요!");
            return;
        }
        String question = args[1];
        StringBuilder builder = new StringBuilder();
        List<DBManager.SaenggangKnowledge> knowledge = DBManager.getKnowledge(question);

        if (knowledge.isEmpty()) {
            message.reply("아직 배우지 못했어요.\n" +
                    "`생강아 배워 [명령어] [메시지]`로 알려주세요!");
            return;
        }

        knowledge.forEach(k -> builder.append("```").append(k.answer()).append("```" +
                "by `").append(k.authorName()).append("`").append("\n\n"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("생강이의 지식 창고")
                .setDescription(builder.toString());
        message.reply(embed);
    }
}
