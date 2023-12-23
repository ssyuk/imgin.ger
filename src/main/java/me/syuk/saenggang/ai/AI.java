package me.syuk.saenggang.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static me.syuk.saenggang.Main.properties;

public class AI {
    public static JsonArray knowledgeContents = new JsonArray();
    public static Map<UUID, Message> aiReplyMap = new HashMap<>();

    public static void updateKnowledgeContents() {
        knowledgeContents = new JsonArray();
        knowledgeContents.add(generateContent("user", "너 이름이 뭐야?"));
        knowledgeContents.add(generateContent("model", "저는 생강이에요."));
        knowledgeContents.add(generateContent("user", "내 이름은 뭔지 알아?"));
        knowledgeContents.add(generateContent("model", "당신은 {user.name}이라고 불러요."));
        knowledgeContents.add(generateContent("user", "내 코인은 얼마야?"));
        knowledgeContents.add(generateContent("model", "당신은 {user.displayCoin}이 있어요."));
        DBManager.getKnowledgeList().forEach((question, saenggangKnowledges) -> {
            for (DBManager.SaenggangKnowledge saenggangKnowledge : saenggangKnowledges) {
                knowledgeContents.add(generateContent("user", question));
                knowledgeContents.add(generateContent("model", saenggangKnowledge.answer()));
            }
        });
    }

    public static JsonObject generateContent(String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", role);
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        parts.add(part);
        content.add("parts", parts);
        return content;
    }

    private static JsonObject newSafetySetting(String category, String threshold) {
        JsonObject safetySetting = new JsonObject();
        safetySetting.addProperty("category", category);
        safetySetting.addProperty("threshold", threshold);
        return safetySetting;
    }

    public static String generateResponse(String prompt, JsonArray moreContents) {
        JsonObject object = new JsonObject();

        JsonArray contents = knowledgeContents.deepCopy();
        contents.addAll(moreContents);
        contents.add(generateContent("user", prompt));
        object.add("contents", contents);

        JsonArray safetySettings = new JsonArray();
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_LOW_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_LOW_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_LOW_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_LOW_AND_ABOVE"));

        HttpURLConnection con = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + properties.getProperty("GEMINI_API_KEY"));

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.getOutputStream().write(object.toString().getBytes());

            JsonObject response = JsonParser.parseReader(new InputStreamReader(con.getInputStream())).getAsJsonObject();

            JsonObject promptFeedback = response.getAsJsonObject("promptFeedback");
            if (promptFeedback.has("blockReason")) {
                return "죄송합니다. 질문이 차단되었습니다. (차단 사유: " + promptFeedback.get("blockReason").getAsString() + ")";
            }

            List<String> answers = new ArrayList<>();
            response.getAsJsonArray("candidates").forEach(candidate -> {
                String text = candidate.getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
                answers.add(text);
            });
            return answers.get((int) (Math.random() * answers.size()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (con != null) con.disconnect();
        }
    }
}
