package me.syuk.saenggang;

import me.syuk.saenggang.commands.Command;
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

        String[] args = content.split(" ");
        Command command = Command.findCommand(args[0]);
        if (command != null) {
            command.execute(user, args, message);
            return;
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
