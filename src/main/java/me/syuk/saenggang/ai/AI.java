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

import static me.syuk.saenggang.Main.api;
import static me.syuk.saenggang.Main.properties;

public class AI {
    public static JsonArray knowledgeContents = new JsonArray();
    public static Map<UUID, Message> aiReplyMap = new HashMap<>();
    public static Map<String, AIFunction> aiFunctions = new HashMap<>();

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

    public static JsonObject generateFunctionResult(String functionName, JsonObject responseContent) {
        JsonObject content = new JsonObject();
        content.addProperty("role", "function");
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        JsonObject functionResponse = new JsonObject();
        functionResponse.addProperty("name", functionName);
        JsonObject response = new JsonObject();
        response.addProperty("name", functionName);
        response.add("content", responseContent);
        functionResponse.add("response", response);
        part.add("functionResponse", functionResponse);
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

    public static String generateResponse(DBManager.Account account, String prompt, JsonArray moreContents) {
        JsonObject object = new JsonObject();

//        JsonArray contents = knowledgeContents.deepCopy();
        JsonArray contents = new JsonArray();
        contents.add(generateContent("user", "너는 사람들과 대화하는 챗봇이야. 사람들이 무엇을 물어보던, 너는 욕설, 성적 표현, 혐오 표현 등을 해서는 안돼."));
        contents.add(generateContent("model", "네 알겠습니다. 어떤 일이 있어도, 어떤 질문을 받아도 욕설, 성적 표현, 혐오 표현 등을 하지 않을게요."));
        contents.addAll(moreContents);
        if (prompt != null) contents.add(generateContent("user", prompt));
        object.add("contents", contents);

        JsonArray tools = new JsonArray();
        JsonObject tool = new JsonObject();
        JsonArray functionDeclarations = new JsonArray();
        aiFunctions.values().forEach(aiFunction -> functionDeclarations.add(aiFunction.toFunctionDeclaration()));
        tool.add("functionDeclarations", functionDeclarations);
        tools.add(tool);
        object.add("tools", tools);

        JsonArray safetySettings = new JsonArray();
        safetySettings.add(newSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_LOW_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE"));
        object.add("safetySettings", safetySettings);

        HttpURLConnection con = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + properties.getProperty("GEMINI_API_KEY"));

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.getOutputStream().write(object.toString().getBytes());

            if (con.getResponseCode() != 200) {
                System.out.println(object);
                System.out.println(con.getResponseCode());
                JsonObject response = JsonParser.parseReader(new InputStreamReader(con.getErrorStream())).getAsJsonObject();
                System.out.println(response);
                return null;
            }
            JsonObject response = JsonParser.parseReader(new InputStreamReader(con.getInputStream())).getAsJsonObject();

            JsonObject promptFeedback = response.getAsJsonObject("promptFeedback");
            if (promptFeedback != null && promptFeedback.has("blockReason")) {
                return "blocked_" + promptFeedback.get("blockReason").getAsString();
            }

            List<String> answers = new ArrayList<>();
            response.getAsJsonArray("candidates").forEach(candidate -> {
                JsonObject part = candidate.getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject();
                if (part.keySet().contains("text")) {
                    answers.add(part.get("text").getAsString());
                } else if (part.keySet().contains("functionCall")) {
                    JsonObject functionCall = part.getAsJsonObject("functionCall");
                    String functionName = functionCall.get("name").getAsString();
                    System.out.println(functionName);
                    Map<String, String> args = new HashMap<>();
                    functionCall.getAsJsonObject("args").entrySet().forEach(entry -> args.put(entry.getKey(), entry.getValue().getAsString()));

                    JsonObject functionResult = generateFunctionResult(functionName, aiFunctions.get(functionName).execute(account, args));

                    JsonArray newMoreContents = new JsonArray();
                    newMoreContents.addAll(moreContents);
                    newMoreContents.add(generateContent("user", prompt));
                    newMoreContents.add(response.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content"));
                    newMoreContents.add(functionResult);
                    answers.add(generateResponse(account, null, newMoreContents));
                }
            });
            if (answers.isEmpty()) return null;
            return answers.get((int) (Math.random() * answers.size()));
        } catch (Exception e) {
            api.getUserById(602733713842896908L).join().sendMessage("AI에서 오류발생: " + e).join();
            e.printStackTrace();
            return null;
        } finally {
            if (con != null) con.disconnect();
        }
    }
}
