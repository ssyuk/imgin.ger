package me.syuk.saenggang.ai.functions.music;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class VolumeFunction implements AIFunction {
    @Override
    public String name() {
        return "music_change_volume";
    }

    @Override
    public String description() {
        return "생강이 음악봇에서, 재생중인 노래의 볼륨을 변경합니다. (0 ~ 100)";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("new_volume", "integer", "변경할 볼륨입니다. (0 ~ 100)", true)
        );
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

        AudioPlayer player = SingingFunction.serverPlayerMap.get(server.getId());
        if (player == null) {
            response.addProperty("error", "재생중인 노래가 없음");
            return response;
        }

        try {
            int volume = Integer.parseInt(args.get("new_volume"));
            if (volume < 0 || volume > 100) {
                response.addProperty("error", "볼륨은 0 ~ 100 사이여야함");
                return response;
            }
            player.setVolume(volume);
            response.addProperty("status", "재생중인 노래 볼륨 변경됨");
            response.addProperty("new_volume", volume);
            return response;
        } catch (NumberFormatException e) {
            response.addProperty("error", "볼륨은 0 ~ 100 사이여야함");
            return response;
        }
    }
}
