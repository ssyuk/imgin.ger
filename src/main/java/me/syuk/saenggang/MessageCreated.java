package me.syuk.saenggang;

import me.syuk.saenggang.games.WordRelay;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.concurrent.CompletableFuture;

public class MessageCreated implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        User user = event.getMessageAuthor().asUser().orElseThrow();
        String content = event.getMessageContent();
        Message message = event.getMessage();

        if (WordRelay.isPlaying(user)) {
            String finalContent = content;
            CompletableFuture.runAsync(() -> {
                Message thinking = message.reply("생각중이에요...").join();
                WordRelay game = WordRelay.playerWordRelayMap.get(user.getIdAsString());
                String lastWord = game.lastWord;
                if (!lastWord.isEmpty()) {
                    char lastChar = lastWord.charAt(lastWord.length() - 1);
                    if (!finalContent.startsWith(String.valueOf(lastChar))) {
                        thinking.edit("틀렸어요! 제가 이겼네요!");
                        game.end(event.getChannel(), false);
                        return;
                    }
                }

                if (finalContent.length() == 1) {
                    thinking.edit("한 글자는 너무 짧아요! 다른 단어를 입력해주세요!");
                    return;
                } else if (finalContent.contains(" ")) {
                    thinking.edit("띄어쓰기는 안돼요! 다른 단어를 입력해주세요!");
                    return;
                } else if (game.usedWords.contains(finalContent)) {
                    thinking.edit("이미 나온 단어에요! 다른 단어를 입력해주세요!");
                    return;
                } else if (!WordRelay.isValidWord(finalContent)) {
                    thinking.edit("사전에 없는 단어에요! 제가 이겼네요!");
                    game.end(event.getChannel(), false);
                    return;
                }
                game.inputWord(finalContent);
                WordRelay.Word nextWord = game.getNextWord(finalContent);
                if (nextWord == null) {
                    thinking.edit("더이상 생각나는게 없어요.. <@" + user.getIdAsString() + ">님이 이겼네요!");
                    game.end(event.getChannel(), true);
                    return;
                }
                game.inputWord(nextWord.word());
                thinking.edit("좋아요. `" + game.lastWord + "`!\n뜻: " + nextWord.meaning());
            });
        }

        if (!content.startsWith("생강아 ")) return;
        content = content.substring(4);

        String[] split = content.split(" ");
        switch (split[0]) {
            case "배워" -> {
                if (split.length != 3) {
                    message.reply("배워 명령어는 `배워 [명령어] [메시지]` 형식으로 사용해주세요!");
                    return;
                }
                String question = split[1];
                String answer = split[2];
                String authorName = event.getMessageAuthor().getName();
                String authorId = event.getMessageAuthor().getIdAsString();

                if (DBManager.getKnown(question) != null) {
                    message.reply("이미 알고있어요..");
                    return;
                }

                DBManager.addKnown(new SaenggangKnown(question, answer, authorName, authorId));
                message.reply("알겟ㅅ슴미다ㅋ `" + question + "`은 `" + answer + "`라거하라구배웟떠요");
                return;
            }
            case "잊어" -> {
                if (split.length != 2) {
                    message.reply("잊어 명령어는 `잊어 [명령어]` 형식으로 사용해주세요!");
                    return;
                }
                String question = split[1];
                SaenggangKnown known = DBManager.getKnown(question);
                if (known == null) {
                    message.reply("그런 명령어는 없어요!");
                    return;
                }
                if (!known.authorId().equals(event.getMessageAuthor().getIdAsString())) {
                    message.reply("알려주신 분만 잊게할 수 있어요!");
                    return;
                }
                DBManager.removeKnown(known);
                message.reply("`" + question + "`이 뭐였죠?");
                return;
            }
            case "끝말잇기" -> {
                WordRelay.start(user);
                message.reply("좋아요. 먼저 시작하세요!");
                return;
            }
        }

        SaenggangKnown known = DBManager.getKnown(content);
        if (known == null) {
            message.reply("그런 명령어는 없어요!");
            return;
        }

        message.reply(known.answer() + "\n" +
                "`" + known.authorName() + "님이 알려주셨어요.`");
    }
}
