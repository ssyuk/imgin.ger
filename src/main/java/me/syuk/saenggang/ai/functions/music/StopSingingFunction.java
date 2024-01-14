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

public class StopSingingFunction implements AIFunction {
    @Override
    public String name() {
        return "music_stop";
    }

    @Override
    public String description() {
        return "생강이 음악봇에서, 재생중인 노래 및 플레이리스트에 있는 노래를 모두 그만 부릅니다. (모두 삭제합니다)";
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

        oChannel.get().disconnect();
        SingingFunction.serverPlayerManagerMap.remove(server.getId());
        SingingFunction.serverPlayerMap.remove(server.getId());
        SingingFunction.serverConnectionMap.remove(server.getId());
        SingingFunction.serverPlaylistMap.remove(server.getId());

        response.addProperty("status", "노래 재생 종료");
        return response;
    }
}
