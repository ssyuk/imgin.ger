package me.syuk.saenggang;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
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
    public static Map<DBManager.Account, ReplyCallback> replyListener = new HashMap<>();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        User user = event.getMessageAuthor().asUser().orElseThrow();
        if (user.isBot()) return;

        DBManager.Account account = DBManager.getAccount(user);

        String content = event.getMessageContent();
        Message message = event.getMessage();

        if (replyListener.containsKey(account)) {
            ReplyCallback callback = replyListener.get(account);
            if (callback.onReply(message)) return;
        }

        if (content.equals("생강아")) {
            message.reply("안녕하세요! 생강이에요.\n" +
                    "`생강아 [할말]`로 저에게 말을 걸 수 있어요.\n" +
                    "`생강아 배워 [명령어] [메시지]`로 저에게 말을 가르칠 수 있어요!"
                + (account.coin() == 0 ? "\n앞으로 저와 재미있게 놀아봐요!" : ""));
            if (account.coin() == 0) account.giveCoin(message.getChannel(), 5, "첫 사용자 보상으로");
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

        List<DBManager.SaenggangKnowledge> knowledge = DBManager.getKnowledge(content);

        if (knowledge.isEmpty()) {
            message.reply("ㄴ네..? 뭐라구요?\n" +
                    "`생강아 배워 [명령어] [메시지]`로 알려주세요!");
            return;
        }

        DBManager.SaenggangKnowledge selectedKnowledge = knowledge.get((int) (Math.random() * knowledge.size()));
        String answer = fixAnswer(selectedKnowledge, account);
        message.reply(answer + "\n" +
                "`" + selectedKnowledge.authorName() + "님이 알려주셨어요.`");
    }

    public static String fixAnswer(DBManager.SaenggangKnowledge selectedKnowledge, DBManager.Account account) {
        String answer = selectedKnowledge.answer();
        answer = answer.replace("{user.name}", "<@" + account.userId() + ">");
        answer = answer.replace("{user.coin}", String.valueOf(account.coin()));
        answer = answer.replace("{user.displayCoin}", Utils.displayCoin(account.coin()));

        answer = answer.replace("\u200B", "");
        answer = answer.replace("`", "");
        answer = answer.replace("@everyone", "@\u200Beveryone");
        answer = answer.replace("@here", "@\u200Bhere");
        answer = answer.replaceAll("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})",
                "`링크`");
        return answer;
    }
}
