package me.syuk.saenggang.ai.functions.music;

import com.google.gson.JsonObject;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class PlaylistFunction implements AIFunction {
    @Override
    public String name() {
        return "music_view_playlist";
    }

    @Override
    public String description() {
        return "생강이 음악봇에서, 현재 재생목록을 보여줘요.";
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

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("플레이리스트 :notes:")
                .setDescription("현재 플레이리스트에 있는 노래 목록입니다.");
        SingingFunction.serverPlaylistMap.get(server.getId()).forEach(audioTrack -> builder.addField(audioTrack.getInfo().title + " - " + audioTrack.getInfo().author, audioTrack.getInfo().uri));
        requestMessage.reply(builder);

        return null;
    }
}
