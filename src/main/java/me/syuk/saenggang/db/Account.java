package me.syuk.saenggang.db;

import me.syuk.saenggang.Utils;
import org.javacord.api.entity.channel.TextChannel;

public record Account(String userId) {
    public int coin() {
        return DBManager.getCoin(userId);
    }

    public void giveCoin(TextChannel channel, int count, String reason) {
        if (!reason.isEmpty()) reason += " ";
        if (count > 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! " + reason + Utils.displayCoin(count) + "을(를) 받았어요. (현재 코인: " + Utils.displayCoin(coin()) + ")");
        } else if (count < 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! " + reason + Utils.displayCoin(-count) + "을(를) 잃었어요. (현재 코인: " + Utils.displayCoin(coin()) + ")");
        }
    }

    public void giveCoin(TextChannel channel, int count) {
        giveCoin(channel, count, "");
    }
}
