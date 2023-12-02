package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class HelpCommand implements Command {
    @Override
    public String name() {
        return "도움말";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        StringBuilder builder = new StringBuilder();
        for (Command command : Command.commands) {
            builder.append("`생강아 ").append(command.name()).append("`\n");
        }
        message.reply(new EmbedBuilder()
                .setTitle("생강이 도움말")
                .setDescription(builder.toString()));
    }
}
