package me.syuk.saenggang;

import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerThreadChannelBuilder;
import org.javacord.api.entity.message.Message;

public class Utils {
    public static ServerThreadChannel createGameThread(Message message, String name) {
        return new ServerThreadChannelBuilder(message, name + " with " + message.getAuthor().getName())
                .setAutoArchiveDuration(AutoArchiveDuration.ONE_HOUR)
                .create().join();
    }

    public static String displayCoin(int coin) {
        return "`\uD83E\uDE99" + coin + "`";
    }
}
