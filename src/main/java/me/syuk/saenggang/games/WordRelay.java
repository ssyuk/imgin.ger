package me.syuk.saenggang.games;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

import static me.syuk.saenggang.Main.properties;

public class WordRelay {
    public static Map<String, WordRelay> playerWordRelayMap = new HashMap<>();
    public String player;
    public String lastWord = "";
    public List<String> usedWords = new ArrayList<>();

    public static boolean isPlaying(User player) {
        return playerWordRelayMap.containsKey(player.getIdAsString());
    }

    public static void start(User player) {
        WordRelay wordRelay = new WordRelay();
        wordRelay.player = player.getIdAsString();
        playerWordRelayMap.put(player.getIdAsString(), wordRelay);
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
        playerWordRelayMap.remove(player);
        channel.sendMessage(new EmbedBuilder()
                .setTitle(playerWin ? "승리!" : "패배..")
                .setDescription(String.join(" -> ", usedWords))
                .setColor(playerWin ? Color.green : Color.red)
                .addField("플레이어", "<@" + player + ">")
                .setFooter("생강이 by syuk")

        );
    }

    public record Word(String word, String meaning) {
    }
}
