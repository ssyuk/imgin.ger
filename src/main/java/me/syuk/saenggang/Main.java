package me.syuk.saenggang;

import me.syuk.saenggang.ai.AI;
import me.syuk.saenggang.ai.functions.account.AttendanceFunction;
import me.syuk.saenggang.ai.functions.account.RankingFunction;
import me.syuk.saenggang.ai.functions.account.ViewCoinFunction;
import me.syuk.saenggang.ai.functions.game.ChosungQuizFunction;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.commands.account.CoinHistoryCommand;
import me.syuk.saenggang.commands.account.SendCoinCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeDrawCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeListCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeSelectCommand;
import me.syuk.saenggang.commands.game.GamblingCommand;
import me.syuk.saenggang.commands.game.KpopQuizCommand;
import me.syuk.saenggang.commands.game.ProverbQuizCommand;
import me.syuk.saenggang.commands.game.WordRelayCommand;
import me.syuk.saenggang.commands.music.*;
import me.syuk.saenggang.commands.owner.GiveCoinCommand;
import me.syuk.saenggang.commands.talking.ForgetCommand;
import me.syuk.saenggang.commands.talking.KnowledgeCommand;
import me.syuk.saenggang.commands.talking.LearnCommand;
import me.syuk.saenggang.commands.utils.HelpCommand;
import me.syuk.saenggang.commands.utils.InviteCommand;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TimeZone;

public class Main {
    public static DiscordApi api;
    public static Properties properties;

    public static void main(String[] args) throws IOException {
        properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get(".properties")));

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

        AI.aiFunctions.put("attendance", new AttendanceFunction());
        Command.commands.add(new CoinHistoryCommand());
        AI.aiFunctions.put("ranking", new RankingFunction());
        Command.commands.add(new SendCoinCommand());
        AI.aiFunctions.put("view_coin", new ViewCoinFunction());

        Command.commands.add(new BadgeDrawCommand());
        Command.commands.add(new BadgeListCommand());
        Command.commands.add(new BadgeSelectCommand());

        AI.aiFunctions.put("chosung_quiz", new ChosungQuizFunction());
        Command.commands.add(new GamblingCommand());
        Command.commands.add(new KpopQuizCommand());
        Command.commands.add(new ProverbQuizCommand());
        Command.commands.add(new WordRelayCommand());

        Command.commands.add(new PlaylistCommand());
        Command.commands.add(new SingingCommand());
        Command.commands.add(new SkipCommand());
        Command.commands.add(new SpeedCommand());
        Command.commands.add(new StopSingingCommand());
        Command.commands.add(new VolumeCommand());

        Command.commands.add(new GiveCoinCommand());

        Command.commands.add(new ForgetCommand());
        Command.commands.add(new KnowledgeCommand());
        Command.commands.add(new LearnCommand());

        Command.commands.add(new HelpCommand());
        Command.commands.add(new InviteCommand());

        DBManager.connect();

        api = new DiscordApiBuilder()
                .setToken(properties.getProperty("BOT_TOKEN"))
                .setWaitForServersOnStartup(false)
                .addIntents(Intent.MESSAGE_CONTENT)
                .login()
                .whenComplete((discordApi, throwable) -> {
                    System.out.println("Logged in as " + discordApi.getYourself().getName());
                    discordApi.getUserById(602733713842896908L).join().sendMessage(discordApi.getYourself().getName() + " is online!").join();
                }).join();

        api.updateActivity(ActivityType.LISTENING, api.getServers().size() + "개의 서버에서 뉴진스의 하입보이");

        api.addServerJoinListener(event -> api.updateActivity(ActivityType.LISTENING, api.getServers().size() + "개의 서버에서 뉴진스의 하입보이"));

        api.addListener(new MessageCreated());
        api.addListener(new ButtonClick());
        api.addListener(new SelectMenuChoose());
    }
}