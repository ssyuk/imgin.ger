package me.syuk.saenggang;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.syuk.saenggang.ai.AI;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.ai.AIResponse;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageReference;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.NonThrowingAutoCloseable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.syuk.saenggang.Main.api;

public class MessageCreated implements MessageCreateListener {
    public static Map<DBManager.Account, ReplyCallback> replyCallbackMap = new HashMap<>();

    public static String fixAnswer(String answer, DBManager.Account account) {
        answer = answer.replace("{user.name}", "<@" + account.userId() + ">");
        answer = answer.replace("{user.coin}", String.valueOf(account.coin()));
        answer = answer.replace("{user.displayCoin}", Utils.displayCoin(account.coin()));

        answer = answer.replace("\u200B", "");
        answer = answer.replace("`", "");
        answer = answer.replace("@everyone", "@\u200Beveryone");
        answer = answer.replace("@here", "@\u200Bhere");
//        answer = answer.replaceAll("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})",
//                "`링크`");
        return answer.trim();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                Optional<User> oUser = event.getMessageAuthor().asUser();
                if (oUser.isEmpty()) return;

                User user = oUser.get();
                if (Utils.isBot(user)) return;

                DBManager.Account account = DBManager.getAccount(user);

                String content = event.getMessageContent();
                Message message = event.getMessage();

                if (replyCallbackMap.containsKey(account)) {
                    ReplyCallback callback = replyCallbackMap.get(account);
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

                if (content.startsWith("!search")) {
                    String query = content.substring(8);
                    message.reply("[Buddy] 잠시만 기다려주세요. Buddy에서 검색하는 중입니다.");
                    try {
                        URL url = new URI("https://oci.syuk.me/generateResponse").toURL();
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setDoOutput(true);
                        JsonObject body = new JsonObject();
                        body.addProperty("query", query);
                        connection.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));
                        connection.setConnectTimeout(60000);

                        JsonObject object = JsonParser.parseReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
                        String response = object.get("response").getAsString();
                        JsonArray embeds = object.getAsJsonArray("embeds");
                        String responseId = object.get("responseId").getAsString();
                        int timeTaken = object.get("timeTaken").getAsInt();

                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(response + "\n```응답 ID: " + responseId + " / 걸린 시간: " + timeTaken / 1000.0 + "초```");
                        for (JsonElement embedElement : embeds) {
                            JsonObject embed = embedElement.getAsJsonObject();
                            messageBuilder.addEmbed(new EmbedBuilder()
                                    .setTitle(embed.get("title").getAsString())
                                    .setUrl(embed.get("url").getAsString())
                                    .setImage(embed.get("image").getAsString())
                            );
                        }
                        messageBuilder.replyTo(event.getMessage()).send(event.getChannel()).join();
                    } catch (URISyntaxException | IOException e) {
                        message.reply("오류가 발생했어요.");
                        throw new RuntimeException(e);
                    }
                    return;
                }


                if (message.getReferencedMessage().isPresent()) {
                    if (!message.getReferencedMessage().get().getAuthor().isYourself()) return;
                } else if (!content.startsWith("생강아 ")) return;

                if (content.startsWith("생강아 ")) content = content.substring(4);

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
                    NonThrowingAutoCloseable typing = message.getChannel().typeContinuously();

                    JsonArray contents = new JsonArray();

                    try {
                        Optional<MessageReference> oReferenced = message.getMessageReference();
                        if (oReferenced.isPresent()) {
                            Message referenced = oReferenced.get().requestMessage().orElseThrow().get();
                            if (referenced.getAuthor().isYourself()) {
                                Optional<MessageReference> oReferencedReferenced = referenced.getMessageReference();
                                if (oReferencedReferenced.isPresent()) {
                                    Message referencedReferenced = oReferencedReferenced.get().requestMessage().orElseThrow().get();
                                    contents.add(AI.generateContent("user", referencedReferenced.getContent()));
                                    contents.add(AI.generateContent("model", referenced.getContent()));
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    AIResponse response = AI.generateResponse(account, message, contents);
                    typing.close();
                    if (response == null) return;

                    String answer = response.content();
                    if (answer.startsWith("blocked_")) {
                        message.reply("죄송합니다. 질문이 차단되었습니다. (차단 사유: " + answer.substring("blocked_".length()) + ")");
                        return;
                    }
                    String tailMessage = "`* AI가 생성한 메시지에요. 올바르지 않은 정보가 담겨있을 수 있어요.`\n" +
                            "`생강아 배워 \"[명령어]\" \"[메시지]\"`로 새로운 지식을 가르쳐주세요!";

                    if (!response.usedFunctions().isEmpty()) {
                        tailMessage = "`* 사용된 기능: " + String.join(", ", response.usedFunctions().stream().map(AIFunction::name).toList()) + "`\n" + tailMessage;
                    }

                    answer = fixAnswer(answer, account);
                    UUID aiReplyId = UUID.randomUUID();
                    MessageBuilder builder = new MessageBuilder();
                    Path path = Paths.get("ai-reply-" + aiReplyId + ".txt");
                    if (answer.length() + tailMessage.length() > 2000) {
                        Files.createFile(path);
                        Files.writeString(path, answer);
                        builder.addAttachment(path.toFile());
                        builder.setContent(tailMessage + "\n\n" +
                                "응답이 너무 길어서 파일로 전송해드렸어요. 아래 파일에서 응답을 확인하실 수 있습니다!");
                    } else {
                        builder.setContent(answer + "\n" + tailMessage);
                    }
                    builder.addComponents(ActionRow.of(
                                    Button.create("ai-regenerate-" + aiReplyId, ButtonStyle.PRIMARY, "", "🔄")
                            ))
                            .replyTo(message).send(message.getChannel()).whenComplete((message1, throwable) -> {
                                if (throwable != null) {
                                    api.getUserById(602733713842896908L).join().sendMessage("AI 응답 출력중 오류발생: " + throwable).join();
                                    throw new RuntimeException(throwable);
                                }
                                AI.aiReplyMap.put(aiReplyId, message1);
                                try {
                                    Files.delete(path);
                                } catch (IOException ignored) {
                                }
                            });
                    return;
                }

                DBManager.SaenggangKnowledge selectedKnowledge = knowledge.get((int) (Math.random() * knowledge.size()));
                String answer = fixAnswer(selectedKnowledge.answer(), account);
                message.reply(answer + "\n" +
                        "`" + selectedKnowledge.authorName() + "님이 알려주셨어요.`");
            } catch (Exception e) {
                api.getUserById(602733713842896908L).join().sendMessage("메시지 처리중 오류발생: " + e).join();
                e.printStackTrace();
            }
        });
    }

    public interface ReplyCallback {
        boolean onReply(Message message);
    }
}
