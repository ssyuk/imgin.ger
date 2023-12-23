package me.syuk.saenggang;

import com.google.gson.JsonArray;
import me.syuk.saenggang.ai.AI;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ButtonClick implements ButtonClickListener {
    public static Map<DBManager.Account, ButtonCallback> buttonCallbackMap = new HashMap<>();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();
        User user = interaction.getUser();
        if (Utils.isBot(user)) return;

        DBManager.Account account = DBManager.getAccount(user);

        if (buttonCallbackMap.containsKey(account)) {
            ButtonCallback callback = buttonCallbackMap.get(account);
            callback.onClick(interaction);
        }
        if (interaction.getCustomId().startsWith("ai-regenerate-")) {
            UUID aiReplyId = UUID.fromString(interaction.getCustomId().substring("ai-regenerate-".length()));
            if (AI.aiReplyMap.containsKey(aiReplyId)) {
                Message message = AI.aiReplyMap.get(aiReplyId);
                Message promptMessage = message.getReferencedMessage().orElseThrow();

                if (promptMessage.getAuthor().getId() != user.getId()) {
                    interaction.createImmediateResponder().setContent("질문한 사람만 응답을 다시 생성할 수 있어요!").setFlags(MessageFlag.EPHEMERAL).respond();
                    return;
                }

                String prompt = promptMessage.getContent().substring(4);
                String answer = AI.generateResponse(prompt, new JsonArray());
                if (answer == null) {
                    message.edit("ㄴ네..? 뭐라구요?\n" +
                            "`생강아 배워 \"[명령어]\" \"[메시지]\"`로 알려주세요!");
                    return;
                }
                message.edit(MessageCreated.fixAnswer(answer, account) + "\n" +
                        "`* AI가 생성한 메시지에요. 올바르지 않은 정보가 담겨있을 수 있어요.`\n" +
                        "`생강아 배워 \"[명령어]\" \"[메시지]\"`로 새로운 지식을 가르쳐주세요!");
                interaction.createImmediateResponder().respond();
            }
        }
    }

    public interface ButtonCallback {
        void onClick(ButtonInteraction interaction);
    }
}
