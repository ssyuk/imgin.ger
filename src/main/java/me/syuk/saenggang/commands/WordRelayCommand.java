package me.syuk.saenggang.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerThreadChannelBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.syuk.saenggang.Main.properties;

public class WordRelayCommand implements Command {
    @Override
    public String name() {
        return "끝말잇기";
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        ServerThreadChannel channel =  new ServerThreadChannelBuilder(message, "생강이와 끝말잇기")
                .create().join();

        WordRelayCommand.WordRelay.start(account);
        channel.sendMessage("좋아요. 먼저 시작하세요!");

        MessageCreated.replyListener.put(account, replyMessage -> {
            if (replyMessage.getChannel().getId() != channel.getId()) return;

            CompletableFuture.runAsync(() -> {
                String content = replyMessage.getContent();
                Message thinking = channel.sendMessage("생각중이에요...").join();
                WordRelay game = WordRelay.playerWordRelayMap.get(account.userId());
                String lastWord = game.lastWord;
                if (!lastWord.isEmpty()) {
                    char lastChar = lastWord.charAt(lastWord.length() - 1);
                    if (!content.startsWith(String.valueOf(lastChar))) {
                        thinking.edit("틀렸어요! 제가 이겼네요!");
                        game.end(channel, false);
                        MessageCreated.replyListener.remove(account);
                        channel.removeThreadMember(Long.parseLong(account.userId()));
                        channel.leaveThread();
                        channel.delete();
                        return;
                    }
                } else {
                    if (game.getNextWord(content) == null) {
                        thinking.edit("시작할땐 한방단어를 사용할 수 없어요. 다른 단어를 입력해주세요!");
                        return;
                    }
                }

                if (content.length() == 1) {
                    thinking.edit("한 글자는 너무 짧아요! 다른 단어를 입력해주세요!");
                    return;
                } else if (content.contains(" ")) {
                    thinking.edit("띄어쓰기는 안돼요! 다른 단어를 입력해주세요!");
                    return;
                } else if (game.usedWords.contains(content)) {
                    thinking.edit("이미 나온 단어에요! 다른 단어를 입력해주세요!");
                    return;
                } else if (!WordRelay.isValidWord(content)) {
                    thinking.edit("사전에 없는 단어에요! 제가 이겼네요!");
                    game.end(channel, false);
                    MessageCreated.replyListener.remove(account);
                    channel.removeThreadMember(Long.parseLong(account.userId()));
                    channel.leaveThread();
                    channel.delete();
                    return;
                }
                game.inputWord(content);
                WordRelay.Word nextWord = game.getNextWord(content);
                if (nextWord == null) {
                    thinking.edit("더이상 생각나는게 없어요.. <@" + account.userId() + ">님이 이겼네요!");
                    game.end(channel, true);
                    MessageCreated.replyListener.remove(account);
                    channel.removeThreadMember(Long.parseLong(account.userId()));
                    channel.leaveThread();
                    channel.delete();
                    return;
                }
                game.inputWord(nextWord.word());
                thinking.edit("좋아요. `" + nextWord.word() + "`!\n뜻: " + nextWord.meaning() + "\n" +
                        "__**" + nextWord.word().charAt(nextWord.word().length() - 1) + "**__(으)로 시작하는 단어를 입력해주세요!");
            });
        });
    }

    public static class WordRelay {
        public static Map<String, WordRelay> playerWordRelayMap = new HashMap<>();
        public Account player;
        public String lastWord = "";
        public List<String> usedWords = new ArrayList<>();

        public static void start(Account player) {
            WordRelay wordRelay = new WordRelay();
            wordRelay.player = player;
            playerWordRelayMap.put(player.userId(), wordRelay);
        }

        public static boolean isValidWord(String word) {
            try {
                String apiUrl = "https://opendict.korean.go.kr/api/search?key=" + properties.getProperty("DICT_API_KEY") + "&req_type=json" +
                        "&type1=word&type3=general&q=" + word;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                JsonObject channel = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject().getAsJsonObject("channel");
                return channel.get("total").getAsInt() > 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Word getNextWord(String word) {
            try {
                String apiUrl = "https://opendict.korean.go.kr/api/search?key=" + properties.getProperty("DICT_API_KEY") + "&req_type=json" +
                        "&pos=1&method=start&type1=word&type3=general&num=100&advanced=y&letter_s=2&sort=popular&q=" + word.charAt(word.length() - 1);
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                JsonArray item = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject().getAsJsonObject("channel").getAsJsonArray("item");
                List<Word> validWords = new ArrayList<>();
                for (JsonElement element : item) {
                    JsonObject object = element.getAsJsonObject();
                    String nextWord = object.get("word").getAsString();
                    if (nextWord.length() < 2) continue;
                    if (nextWord.contains("-")) continue;
                    if (usedWords.contains(nextWord)) continue;
                    validWords.add(new Word(nextWord, object.getAsJsonArray("sense").get(0).getAsJsonObject().get("definition").getAsString()));
                }
                if (validWords.isEmpty()) return null;
                return validWords.get(new Random().nextInt(validWords.size()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void inputWord(String word) {
            usedWords.add(word);
            lastWord = word;
        }

        public void end(TextChannel channel, boolean playerWin) {
            playerWordRelayMap.remove(player.userId());
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(playerWin ? "승리!" : "패배..")
                    .setDescription(String.join(" -> ", usedWords))
                    .setColor(playerWin ? Color.green : Color.red)
                    .addField("플레이어", "<@" + player.userId() + ">")
                    .setFooter("생강이 by syuk")
            );

            if (playerWin) {
                int coin = usedWords.size() / 4;
                if (coin == 0) {
                    channel.sendMessage("<@" + player.userId() + ">님! 코인을 얻지 못했어요. 더 길게 이어보세요!");
                    return;
                }
                player.giveCoin(channel, coin);
            }
        }

        public record Word(String word, String meaning) {
        }
    }
}
