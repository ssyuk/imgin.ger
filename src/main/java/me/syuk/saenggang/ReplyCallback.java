package me.syuk.saenggang;

import org.javacord.api.entity.message.Message;

public interface ReplyCallback {
    void onReply(Message message);
}
