package me.syuk.saenggang.ai.functions.account;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.List;
import java.util.Map;

public class ViewCoinFunction implements AIFunction {
    @Override
    public String name() {
        return "view_coin";
    }

    @Override
    public String description() {
        return "A function to check how many coins I (or someone else) currently have.";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("user", "string", "ID of the user to be confirmed (The id is provided in the format <@ID>. If you can't find it in the message, leave it blank. The system will automatically mark this message as the user who entered it.)", false)
        );
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        JsonObject content = new JsonObject();

        DBManager.Account target = account;
        if (args.containsKey("user") && !args.get("user").equals("me")) {
            try {
                String user = args.get("user");
                if (user.startsWith("<@") && user.endsWith(">")) {
                    user = user.substring(2, user.length() - 1);
                    if (user.startsWith("!")) user = user.substring(1);
                }
                target = new DBManager.Account(Long.parseLong(user));
            } catch (NumberFormatException e) {
                content.addProperty("failed_reason", "사용자를 태그(@)해주세요!");
                return content;
            }
        }

        content.addProperty("coin", target.coin());
        return content;
    }
}
