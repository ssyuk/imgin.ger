package me.syuk.saenggang.ai.functions;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceFunction implements AIFunction {
    @Override
    public String name() {
        return "attendance";
    }

    @Override
    public String description() {
        return "You can earn coins by checking attendance once a day. The amount of coins you receive varies depending on the order.";
    }

    @Override
    public List<Parameter> parameters() {
        return new ArrayList<>();
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args) {
        JsonObject content = new JsonObject();

        if (DBManager.isAttended(account)) {
            content.addProperty("failed_reason", "이미 출석했어요!");
            return content;
        }

        DBManager.AttendStatus status = DBManager.attend(account);
        content.addProperty("ranking", status.ranking());
        content.addProperty("streak", status.streak());

        if (status.ranking() == 1) {
            content.addProperty("coin", 15);
            DBManager.giveCoin(account, 15);
        }
        else if (status.ranking() == 2) {
            content.addProperty("coin", 10);
            DBManager.giveCoin(account, 10);
        }
        else if (status.ranking() == 3) {
            content.addProperty("coin", 7);
            DBManager.giveCoin(account, 7);
        }
        else {
            content.addProperty("coin", 5);
            DBManager.giveCoin(account, 5);
        }
        return content;
    }
}
