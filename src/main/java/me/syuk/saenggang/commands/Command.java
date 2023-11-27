package me.syuk.saenggang.commands;

import me.syuk.saenggang.Account;
import org.javacord.api.entity.message.Message;

import java.util.ArrayList;
import java.util.List;

public interface Command {
    List<Command> commands = new ArrayList<>();

    String name();
    void execute(Account account, String[] args, Message message);

    static Command findCommand(String name) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
