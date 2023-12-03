package me.syuk.saenggang;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.util.HashMap;
import java.util.Map;

public class ButtonClick implements ButtonClickListener {
    public static Map<DBManager.Account, ButtonCallback> buttonCallbackMap = new HashMap<>();
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();
        User user = interaction.getUser();
        if (user.isBot()) return;

        DBManager.Account account = DBManager.getAccount(user);

        if (buttonCallbackMap.containsKey(account)) {
            ButtonCallback callback = buttonCallbackMap.get(account);
            callback.onClick(interaction);
        }
    }

    public interface ButtonCallback {
        void onClick(ButtonInteraction interaction);
    }
}
