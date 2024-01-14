package me.syuk.saenggang.ai.functions.music;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.*;

import static me.syuk.saenggang.Main.api;

public class SpeedFunction implements AIFunction {
    public static Map<Long, Double> serverMusicSpeedMap = new HashMap<>();

    @Override
    public String name() {
        return "music_change_speed";
    }

    @Override
    public String description() {
        return "생강이 음악봇에서, 재생중인(부르고있는) 노래의 속도를 변경합니다. (0.1 ~ 5.0)";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("new_speed", "double", "변경할 속도입니다. (0.1 ~ 5.0)", true)
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
            double speed = Double.parseDouble(args.get("new_speed"));
            if (speed < 0.1 || speed > 5) {
                response.addProperty("error", "속도는 0.1~5 사이여야함");
                return response;
            }
            changeSpeed(server, player, speed);
            response.addProperty("status", "재생중인 노래 속도 변경됨");
            return response;
        } catch (Exception e) {
            response.addProperty("error", "속도는 0.1~5 사이여야함");
            return response;
        }
    }

    private void changeSpeed( Server server, AudioPlayer player, double speed) {
        player.setFilterFactory((track, format, output) -> {
            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            audioFilter.setSpeed(speed);
            return Collections.singletonList(audioFilter);
        });
        serverMusicSpeedMap.put(server.getId(), speed);
    }
}
