package me.syuk.saenggang.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        return generateContent(role, text, null);
    }

    public static JsonObject generateContent(String role, String text, String image) {
        JsonObject content = new JsonObject();
        content.addProperty("role", role);
        JsonArray parts = new JsonArray();

        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        parts.add(part);

        if (image != null) {
            JsonObject imagePart = new JsonObject();
            JsonObject inlineData = new JsonObject();
            inlineData.addProperty("mime_type", "image/jpeg");
            inlineData.addProperty("data", image);
            imagePart.add("inline_data", inlineData);
            parts.add(imagePart);
        }

        content.add("parts", parts);
        return content;
    }

    public static JsonObject generateFunctionResult(String functionName, JsonObject responseContent) {
        if (responseContent == null) return null;

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

    public static String generateResponse(DBManager.Account account, Message requestMessage, JsonArray moreContents) {
        JsonObject object = new JsonObject();
        String prompt = null;
        boolean hasImage = false;

        JsonArray contents = new JsonArray();
        contents.add(generateContent("user", "너는 사람들과 대화하는 챗봇이야. 사람들이 무엇을 물어보던, 너는 욕설, 성적 표현, 혐오 표현, 정치적 표현 등을 하지 않아야해."));
        contents.add(generateContent("model", "ㅇㅋ. 그럼 욕설, 성적 표현, 혐오 표현, 정치적 표현 등은 절때 안할게"));
        contents.add(generateContent("user", "너는 또한 사용자와 친근하게 대화해야해. 완전 친한 친구처럼 반말 써도 돼!"));
        contents.add(generateContent("model", "그래 친근하게 말할겡~"));
        contents.add(generateContent("user", "그럼 이제 시작해볼까?"));
        contents.add(generateContent("model", "그래! 먼저 말걸어줘!"));
        contents.addAll(moreContents);
        if (requestMessage != null) {
            prompt = requestMessage.getContent();
            if (prompt.startsWith("생강아 ")) prompt = prompt.substring(4);

            String imageData = null;
            if (!requestMessage.getAttachments().isEmpty()) {
                if (requestMessage.getAttachments().size() <= 1) {
                    try (InputStream inputStream = requestMessage.getAttachments().get(0).getUrl().openStream()) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        imageData = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else requestMessage.reply("이미지는 한장만 보내주세요.");

            }

            hasImage = imageData != null;
            if (hasImage) contents = new JsonArray();

            contents.add(generateContent("user", prompt, imageData));
        }
        object.add("contents", contents);

        if (!hasImage) {
            JsonArray tools = new JsonArray();
            JsonObject tool = new JsonObject();
            JsonArray functionDeclarations = new JsonArray();
            aiFunctions.values().forEach(aiFunction -> functionDeclarations.add(aiFunction.toFunctionDeclaration()));
            tool.add("functionDeclarations", functionDeclarations);
            tools.add(tool);
            object.add("tools", tools);
        }

        JsonArray safetySettings = new JsonArray();
        safetySettings.add(newSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_LOW_AND_ABOVE"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"));
        safetySettings.add(newSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE"));
        object.add("safetySettings", safetySettings);

        HttpURLConnection con = null;
        try {
            String model = hasImage ? "gemini-pro-vision" : "gemini-pro";
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + properties.getProperty("GEMINI_API_KEY"));

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
            String finalPrompt = prompt;
            response.getAsJsonArray("candidates").forEach(candidate -> {
                if (candidate.getAsJsonObject().get("finishReason").getAsString().equals("SAFETY")) {
                    answers.add("blocked_죄송합니다. AI가 안전하지 않은 메시지를 생성했습니다. 다시 시도해주세요.");
                    return;
                }
                JsonObject part = candidate.getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject();
                if (part.keySet().contains("text")) {
                    answers.add(part.get("text").getAsString());
                } else if (part.keySet().contains("functionCall")) {
                    JsonObject functionCall = part.getAsJsonObject("functionCall");
                    String functionName = functionCall.get("name").getAsString();
                    Map<String, String> args = new HashMap<>();
                    functionCall.getAsJsonObject("args").entrySet().forEach(entry -> args.put(entry.getKey(), entry.getValue().getAsString()));

                    AIFunction function = aiFunctions.get(functionName);
                    if (function == null) {
                        System.err.println("AI에 등록되지 않은 함수가 호출되었습니다: " + functionName);
                        return;
                    }
                    JsonObject functionResult = generateFunctionResult(functionName, function.execute(account, args, requestMessage));

                    if (functionResult != null) {
                        JsonArray newMoreContents = new JsonArray();
                        newMoreContents.addAll(moreContents);
                        newMoreContents.add(generateContent("user", finalPrompt));
                        newMoreContents.add(response.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content"));
                        newMoreContents.add(functionResult);
                        answers.add(generateResponse(account, null, newMoreContents));
                    }
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
