package me.syuk.saenggang;

import org.javacord.api.entity.channel.AutoArchiveDuration;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;

public class Utils {
    public static ServerThreadChannel createGameThread(Message message, String name) {
        return message.createThread(name + " with " + message.getAuthor().getName(), AutoArchiveDuration.ONE_HOUR).join();
    }

    public static String displayCoin(int coin) {
        return "`\uD83E\uDE99" + coin + "`";
    }
}
