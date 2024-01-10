package me.syuk.saenggang.ai.functions;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;

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
                new Parameter("user", "string", "ID of the user to be confirmed (id is given in <@ID> format, or if it means ‘me’, it is unified as ‘me’.)", false)
        );
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args) {
        JsonObject content = new JsonObject();

        DBManager.Account target = account;
        if (args.containsKey("user") && !args.get("user").equals("me")) {
            try {
                target = new DBManager.Account(Long.parseLong(args.get("user")));
            } catch (NumberFormatException e) {
                content.addProperty("failed_reason", "사용자를 태그(@)해주세요!");
                return content;
            }
        }

        content.addProperty("coin", target.coin());
        return content;
    }
}
