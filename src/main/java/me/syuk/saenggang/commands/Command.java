package me.syuk.saenggang.commands;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.List;

public interface Command {
    enum Theme {
        ACCOUNT, GAME, FOR_OWNER, UTILS, TALKING, MUSIC, COSMETIC
    }
    List<Command> commands = new ArrayList<>();

    String name();
    Theme theme();
    void execute(DBManager.Account account, String[] args, Message message);

    static Command findCommand(String name) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
