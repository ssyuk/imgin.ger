package me.syuk.saenggang;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.commands.ForgetCommand;
import me.syuk.saenggang.commands.LearnCommand;
import me.syuk.saenggang.commands.WordRelayCommand;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static DiscordApi api;
    public static Properties properties;

    public static void main(String[] args) throws IOException {
        properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get(".properties")));

        Command.commands.add(new LearnCommand());
        Command.commands.add(new ForgetCommand());
        Command.commands.add(new WordRelayCommand());

        DBManager.connect();

        api = new DiscordApiBuilder()
                .setToken(properties.getProperty("BOT_TOKEN"))
                .setWaitForServersOnStartup(false)
                .addIntents(Intent.MESSAGE_CONTENT)
                .login().join();

        api.updateActivity(ActivityType.LISTENING, "뉴진스의 하입보이");

        api.addListener(new MessageCreated());
    }
}