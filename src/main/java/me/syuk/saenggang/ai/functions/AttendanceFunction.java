package me.syuk.saenggang.ai.functions;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.List;
import java.util.Map;

public class AttendanceFunction implements AIFunction {
    @Override
    public String name() {
        return "attendance";
    }

    @Override
    public String description() {
        return "매일 한번씩 사용하는 명령어. 이 명령어를 사용하면, 전체중 사용한 순서에 따라 다른 코인을 받을 수 있음. 하루에 한번씩만 사용 가능함. ex) 출첵, 출석체크 등";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public boolean isTalkingFunction() {
        return true;
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        JsonObject content = new JsonObject();

        if (DBManager.isAttended(account)) {
            content.addProperty("error", "이미 출석했어요!");
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
