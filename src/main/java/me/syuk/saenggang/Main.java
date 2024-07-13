package me.syuk.saenggang;

import me.syuk.saenggang.ai.AI;
import me.syuk.saenggang.ai.functions.account.AttendanceFunction;
import me.syuk.saenggang.ai.functions.account.RankingFunction;
import me.syuk.saenggang.ai.functions.account.ViewCoinFunction;
import me.syuk.saenggang.ai.functions.game.*;
import me.syuk.saenggang.ai.functions.music.*;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.commands.account.CoinHistoryCommand;
import me.syuk.saenggang.commands.account.SendCoinCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeDrawCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeListCommand;
import me.syuk.saenggang.commands.cosmetic.BadgeSelectCommand;
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
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

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

        AI.registerFunction(new AttendanceFunction());
        Command.commands.add(new CoinHistoryCommand());
        AI.registerFunction(new RankingFunction());
        Command.commands.add(new SendCoinCommand());
        AI.registerFunction(new ViewCoinFunction());

        Command.commands.add(new BadgeDrawCommand());
        Command.commands.add(new BadgeListCommand());
        Command.commands.add(new BadgeSelectCommand());

        AI.registerFunction(new ChosungQuizFunction());
        AI.registerFunction(new GamblingFunction());
        AI.registerFunction(new KpopQuizFunction());
        AI.registerFunction(new ProverbQuizFunction());
        AI.registerFunction(new WordRelayFunction());

        AI.registerFunction(new PlaylistFunction());
        AI.registerFunction(new SingingFunction());
        AI.registerFunction(new SkipFunction());
        AI.registerFunction(new SpeedFunction());
        AI.registerFunction(new StopSingingFunction());
        AI.registerFunction(new VolumeFunction());

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

        api.addInteractionCreateListener(interactionCreateEvent -> {
            SlashCommandInteraction interaction = interactionCreateEvent.getInteraction().asSlashCommandInteraction().orElseThrow();
            String query = interaction.getArgumentByName("query").orElseThrow().getStringValue().orElseThrow();
            InteractionOriginalResponseUpdater updater = interaction.createImmediateResponder().setContent("잠시만 기다려주세요.").respond().join();


        });
    }
}