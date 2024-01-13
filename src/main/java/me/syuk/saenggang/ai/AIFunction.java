package me.syuk.saenggang.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.List;
import java.util.Map;

public interface AIFunction {
    String name();

    String description();

    List<Parameter> parameters();

    boolean isTalkingFunction();

    default JsonObject toFunctionDeclaration() {
        JsonObject function = new JsonObject();
        function.addProperty("name", name());
        function.addProperty("description", description());

        if (!parameters().isEmpty()) {
            JsonObject parameters = new JsonObject();
            parameters.addProperty("type", "object");
            JsonObject properties = new JsonObject();
            for (Parameter parameter : parameters()) {
                JsonObject property = new JsonObject();
                property.addProperty("type", parameter.type);
                property.addProperty("description", parameter.description);
                properties.add(parameter.name, property);
            }
            parameters.add("properties", properties);

            JsonArray required = new JsonArray();
            parameters().stream().filter(parameter -> parameter.required).forEach(parameter -> required.add(parameter.name));
            parameters.add("required", required);

            function.add("parameters", parameters);
        }

        return function;
    }

    JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage);

    record Parameter(String name, String type, String description, boolean required) {
    }
}
