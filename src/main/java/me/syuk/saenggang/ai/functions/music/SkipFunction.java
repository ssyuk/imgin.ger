package me.syuk.saenggang.ai.functions.music;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class SkipFunction implements AIFunction {
    @Override
    public String name() {
        return "music_skip";
    }

    @Override
    public String description() {
        return "생강이 음악봇에서, 재생중인 노래를 스킵하고 다음 노래를 재생함.";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        JsonObject response = new JsonObject();

        if (requestMessage.getServer().isEmpty()) {
            response.addProperty("error", "서버에서만 노래기능 사용 가능");
            return response;
        }
        Server server = requestMessage.getServer().get();

        Optional<ServerVoiceChannel> oChannel = api.getYourself().getConnectedVoiceChannel(server);
        if (oChannel.isEmpty()) {
            response.addProperty("error", "재생중인 노래가 없음");
            return response;
        }

        SingingFunction.serverPlayerMap.get(server.getId()).stopTrack();
        response.addProperty("status", "재생중인 노래 스킵됨");
        return response;
    }
}
