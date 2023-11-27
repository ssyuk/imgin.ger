package me.syuk.saenggang.commands;

import me.syuk.saenggang.games.WordRelay;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

public class WordRelayCommand implements Command {
    @Override
    public String name() {
        return "끝말잇기";
    }

    @Override
    public void execute(User user, String[] args, Message message) {
        WordRelay.start(user);
        message.reply("좋아요. 먼저 시작하세요!");
    }
}
