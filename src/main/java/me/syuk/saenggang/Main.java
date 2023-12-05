package me.syuk.saenggang;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.commands.account.AttendanceCommand;
import me.syuk.saenggang.commands.account.CoinHistoryCommand;
import me.syuk.saenggang.commands.account.RankingCommand;
import me.syuk.saenggang.commands.account.WalletCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeDrawCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeListCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeSelectCommand;
import me.syuk.saenggang.commands.game.*;
import me.syuk.saenggang.commands.owner.GiveCoinCommand;
import me.syuk.saenggang.commands.talking.ForgetCommand;
import me.syuk.saenggang.commands.talking.KnowledgeCommand;
import me.syuk.saenggang.commands.talking.LearnCommand;
import me.syuk.saenggang.commands.utils.HelpCommand;
import me.syuk.saenggang.db.DBManager;
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

        Command.commands.add(new AttendanceCommand());
        Command.commands.add(new CoinHistoryCommand());
        Command.commands.add(new RankingCommand());
        Command.commands.add(new WalletCommand());

        Command.commands.add(new BadgeDrawCommand());
        Command.commands.add(new BadgeListCommand());
        Command.commands.add(new BadgeSelectCommand());

        Command.commands.add(new ChosungQuizCommand());
        Command.commands.add(new GamblingCommand());
        Command.commands.add(new KpopQuizCommand());
        Command.commands.add(new ProverbQuizCommand());
        Command.commands.add(new WordRelayCommand());

        Command.commands.add(new GiveCoinCommand());

        Command.commands.add(new ForgetCommand());
        Command.commands.add(new KnowledgeCommand());
        Command.commands.add(new LearnCommand());

        Command.commands.add(new HelpCommand());

        DBManager.connect();

        api = new DiscordApiBuilder()
                .setToken(properties.getProperty("BOT_TOKEN"))
                .setWaitForServersOnStartup(false)
                .addIntents(Intent.MESSAGE_CONTENT)
                .login()
                .whenComplete((discordApi, throwable) -> System.out.println("Logged in as " + discordApi.getYourself().getName())).join();

        api.updateActivity(ActivityType.LISTENING, "뉴진스의 하입보이");

        api.addListener(new MessageCreated());
        api.addListener(new ButtonClick());
        api.addListener(new SelectMenuChoose());
    }
}