package me.syuk.saenggang.commands.utils;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class InviteCommand implements Command {
    @Override
    public String name() {
        return "초대";
    }

    @Override
    public Theme theme() {
        return Theme.UTILS;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(":door: 생강이 초대하기")
                .setDescription("생강이를 초대하고 싶으신가요? 아래 링크를 클릭해주세요!")
                .addField("초대 링크", "[여기를 클릭하세요!](https://discord.com/api/oauth2/authorize?client_id=1114900908816482336&permissions=395137362944&scope=bot%20applications.commands)");
        message.reply(builder);
    }
}
