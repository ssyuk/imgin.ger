package me.syuk.saenggang.commands;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.List;

public interface Command {
    List<Command> commands = new ArrayList<>();

    String name();
    void execute(User user, String[] args, Message message);

    static Command findCommand(String name) {
        for (Command command : commands) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }
}
