package me.syuk.saenggang.ai.functions.game;

import app.myoun.headsound.HeadSound;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static me.syuk.saenggang.Main.properties;

public class WordRelayFunction implements AIFunction {
    @Override
    public String name() {
        return "word_relay";
    }

    @Override
    public String description() {
        return "Start word relay(끝말잇기) game";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public JsonObject execute(DBManager.Account account,Map<String, String> args, Message requestMessage) {
        ServerThreadChannel channel = Utils.createGameThread(requestMessage, "끝말잇기");

        WordRelay.start(account);
        channel.sendMessage("""
                끝말잇기는 제시된 단어의 끝 글자로 시작하는 단어를 입력하는 게임입니다.
                예를 들어, `사과`라는 단어가 나왔다면 `과일`라는 단어를 입력할 수 있습니다.
                단어는 사전에 있는 단어여야 하며, 한글자 단어와 띄어쓰기가 포함되는 단어는 사용할 수 없습니다.
                **승리했을때, 이어진 단어당 2코인을 얻을 수 있습니다.**
                """);
        channel.sendMessage("좋아요. 먼저 시작하세요!");

        MessageCreated.replyCallbackMap.put(account, replyMessage -> {
            if (replyMessage.getChannel().getId() != channel.getId()) return false;

            String content = replyMessage.getContent();
            Message thinking = channel.sendMessage("생각중이에요...").join();
            WordRelay game = WordRelay.playerWordRelayMap.get(account.userId());
            String lastWord = game.lastWord;
            try {
                boolean result = CompletableFuture.supplyAsync(() -> {
                    if (!lastWord.isEmpty()) {
                        char lastChar = lastWord.charAt(lastWord.length() - 1);
                        char lastCharWithHeadSound = HeadSound.transform(lastChar);
                        if (!content.startsWith(String.valueOf(lastChar)) && !content.startsWith(String.valueOf(lastCharWithHeadSound))) {
                            thinking.edit("틀렸어요! 제가 이겼네요!");
                            game.end(channel, "틀렸어요!", false);
                            MessageCreated.replyCallbackMap.remove(account);
                            channel.createUpdater().setArchivedFlag(true).update();
                            return true;
                        }
                    } else {
                        if (game.getNextWords(content).isEmpty()) {
                            thinking.edit("시작할땐 한방단어를 사용할 수 없어요. 다른 단어를 입력해주세요!");
                            return true;
                        }
                    }

                    if (content.length() == 1) {
                        thinking.edit("한 글자는 너무 짧아요! 다른 단어를 입력해주세요!");
                        return true;
                    } else if (content.contains(" ")) {
                        thinking.edit("띄어쓰기는 안돼요! 다른 단어를 입력해주세요!");
                        return true;
                    } else if (game.usedWords.contains(content)) {
                        thinking.edit("이미 나온 단어에요! 다른 단어를 입력해주세요!");
                        return true;
                    } else if (!WordRelay.isValidWord(content)) {
                        thinking.edit("사전에 없는 단어에요! 제가 이겼네요!");
                        game.end(channel, "사전에 없는 단어에요!", false);
                        MessageCreated.replyCallbackMap.remove(account);
                        channel.createUpdater().setArchivedFlag(true).update();
                        return true;
                    }
                    game.inputWord(content);

                    List<WordRelay.Word> nextWords = game.getNextWords(content);
                    if (nextWords.isEmpty()) {
                        thinking.edit("더이상 생각나는게 없어요.. <@" + account.userId() + ">님이 이겼네요!");
                        game.end(channel, "더이상 생각나는게 없어요..", true);
                        MessageCreated.replyCallbackMap.remove(account);
                        channel.createUpdater().setArchivedFlag(true).update();
                        return true;
                    }
                    WordRelay.Word nextWord = nextWords.get((int) (Math.random() * nextWords.size()));
                    game.inputWord(nextWord.word());
                    char nextChar = nextWord.word().charAt(nextWord.word().length() - 1);
                    char nextCharWithHeadSound = HeadSound.transform(nextChar);
                    String nextCharString = String.valueOf(nextChar);
                    if (nextCharWithHeadSound != nextChar) nextCharString += " 또는 " + nextCharWithHeadSound;
                    thinking.edit("좋아요. `" + nextWord.word() + "`!\n뜻: " + nextWord.meaning() + "\n" +
                            "__**" + nextCharString + "**__(으)로 시작하는 단어를 입력해주세요!");
                    return true;
                }).completeOnTimeout(false, 10, TimeUnit.SECONDS).get();

                if (!result) {
                    thinking.edit("더이상 생각나는게 없어요.. <@" + account.userId() + ">님이 이겼네요!");
                    game.end(channel, "더이상 생각나는게 없어요..", true);
                    MessageCreated.replyCallbackMap.remove(account);
                    channel.createUpdater().setArchivedFlag(true).update();
                }
            } catch (InterruptedException | ExecutionException e) {
                thinking.edit("죄송해요. 오류가 발생했어요. <@" + account.userId() + ">님이 이겼네요!");
                game.end(channel, "오류가 발생했어요.", true);
                MessageCreated.replyCallbackMap.remove(account);
                channel.createUpdater().setArchivedFlag(true).update();
            }
            return true;
        });

        JsonObject response = new JsonObject();
        response.addProperty("content", "위에 있는 Thread에서 게임을 시작함.");
        return response;
    }

    public static class WordRelay {
        public static Map<Long, WordRelay> playerWordRelayMap = new HashMap<>();
        public DBManager.Account player;
        public String lastWord = "";
        public List<String> usedWords = new ArrayList<>();

        public static void start(DBManager.Account player) {
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

        public List<Word> getNextWords(String word) {
            char lastChar = word.charAt(word.length() - 1);
            try {
                String apiUrl = "https://opendict.korean.go.kr/api/search?key=" + properties.getProperty("DICT_API_KEY") + "&req_type=json" +
                        "&pos=1&method=start&type1=word&type3=general&num=100&advanced=y&letter_s=2&sort=popular&q=" + lastChar;
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

                if (HeadSound.transform(lastChar) != lastChar && validWords.isEmpty())
                    validWords.addAll(getNextWords(String.valueOf(HeadSound.transform(lastChar))));
                return validWords;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void inputWord(String word) {
            usedWords.add(word);
            lastWord = word;
        }

        public void end(TextChannel channel, String reason, boolean playerWin) {
            playerWordRelayMap.remove(player.userId());
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(playerWin ? "승리!" : "패배..")
                    .setDescription("**사유**: " + reason + "\n" +
                            String.join(" -> ", usedWords))
                    .setColor(playerWin ? Color.green : Color.red)
                    .addField("플레이어", "<@" + player.userId() + ">")
                    .setFooter("생강이 by syuk")
            );

            if (playerWin) {
                if (usedWords.size() < 4) {
                    channel.sendMessage("<@" + player.userId() + ">님! 코인을 얻지 못했어요. 더 길게 이어보세요!");
                    return;
                }
                int coin = usedWords.size() * 2;
                player.giveCoin(channel, coin);
            }
        }

        public record Word(String word, String meaning) {
        }
    }
}
