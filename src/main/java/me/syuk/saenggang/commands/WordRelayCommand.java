package me.syuk.saenggang.commands;

import me.syuk.saenggang.Account;
import me.syuk.saenggang.games.WordRelay;
import org.javacord.api.entity.message.Message;

public class WordRelayCommand implements Command {
    @Override
    public String name() {
        return "끝말잇기";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        WordRelay.start(account);
        message.reply("좋아요. 먼저 시작하세요!");
    }
}
