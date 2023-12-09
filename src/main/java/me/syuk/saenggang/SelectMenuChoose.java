package me.syuk.saenggang;

import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import java.util.HashMap;
import java.util.Map;

public class SelectMenuChoose implements SelectMenuChooseListener {
    public static Map<DBManager.Account, SelectMenuCallback> selectMenuCallbackMap = new HashMap<>();
    @Override
    public void onSelectMenuChoose(SelectMenuChooseEvent event) {
        SelectMenuInteraction interaction = event.getSelectMenuInteraction();
        User user = interaction.getUser();
        if (Utils.isBot(user)) return;

        DBManager.Account account = DBManager.getAccount(user);

        if (selectMenuCallbackMap.containsKey(account)) {
            SelectMenuCallback callback = selectMenuCallbackMap.get(account);
            callback.onSelect(interaction);
        }
    }

    public interface SelectMenuCallback {
        void onSelect(SelectMenuInteraction interaction);
    }
}
