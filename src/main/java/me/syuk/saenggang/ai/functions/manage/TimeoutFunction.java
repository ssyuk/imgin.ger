package me.syuk.saenggang.ai.functions.manage;

import com.google.gson.JsonObject;
import me.syuk.saenggang.Main;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TimeoutFunction implements AIFunction {
    @Override
    public String name() {
        return "set_user_timeout";
    }

    @Override
    public String description() {
        return "A function to set a timeout for a user.";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("user", "string", "ID of the user to be timed out", true),
                new Parameter("time", "string", "Time to timeout the user (in seconds)", true)
        );
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        JsonObject content = new JsonObject();

        if (!args.containsKey("user")) {
            content.addProperty("failed_reason", "Please tag the user.");
            return content;
        }

        try {
            String user = args.get("user");
            if (user.startsWith("<@") && user.endsWith(">")) {
                user = user.substring(2, user.length() - 1);
                if (user.startsWith("!")) user = user.substring(1);
            }
            requestMessage.getServer().get().timeoutUser(Main.api.getUserById(user).get(), Duration.ofSeconds(Long.parseLong(args.get("time")))).get();
            content.addProperty("success", "The user has been timed out.");
        } catch (NumberFormatException e) {
            content.addProperty("failed_reason", "Please tag the user.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return content;
    }
}
