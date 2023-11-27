package me.syuk.saenggang.db;

import org.javacord.api.entity.channel.TextChannel;

public record Account(String userId) {
    public int coin() {
        return DBManager.getCoin(userId);
    }

    public void giveCoin(TextChannel channel, int count, String reason) {
        if (count > 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! " + reason + " `\uD83E\uDE99" + count + "`을(를) 받았어요.");
        } else if (count < 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! " + reason + " `\uD83E\uDE99" + -count + "`을(를)잃었어요.");
        }
    }

    public void giveCoin(TextChannel channel, int count) {
        if (count > 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! `\uD83E\uDE99" + count + "`을(를) 받았어요.");
        } else if (count < 0) {
            DBManager.giveCoin(this, count);
            channel.sendMessage("<@" + userId + ">님! `\uD83E\uDE99" + -count + "`을(를) 잃었어요.");
        }
    }
}
