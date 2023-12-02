package me.syuk.saenggang;

import org.javacord.api.entity.message.Message;

public interface ReplyCallback {
    boolean onReply(Message message);
}
