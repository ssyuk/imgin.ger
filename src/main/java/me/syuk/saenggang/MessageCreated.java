package me.syuk.saenggang;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.Account;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.db.SaenggangKnown;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCreated implements MessageCreateListener {
    public static Map<Account, ReplyCallback> replyListener = new HashMap<>();
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        User user = event.getMessageAuthor().asUser().orElseThrow();
        if (user.isBot()) return;

        Account account = DBManager.getAccount(user);

        String content = event.getMessageContent();
        Message message = event.getMessage();

        if (replyListener.containsKey(account)) {
            ReplyCallback callback = replyListener.get(account);
            callback.onReply(message);
            return;
        }

        if (!content.startsWith("생강아 ")) return;
        content = content.substring(4);

        List<String> args = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(content);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                args.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                args.add(regexMatcher.group(2));
            } else {
                args.add(regexMatcher.group());
            }
        }
        Command command = Command.findCommand(args.get(0));
        if (command != null) {
            command.execute(account, args.toArray(String[]::new), message);
            return;
        }

        SaenggangKnown known = DBManager.getKnown(content);
        if (known == null) {
            message.reply("ㄴ네..? 뭐라구요?\n" +
                    "`생강아 배워 [명령어] [메시지]`로 알려주세요!");
            return;
        }

        message.reply(known.answer() + "\n" +
                "`" + known.authorName() + "님이 알려주셨어요.`");
    }
}
