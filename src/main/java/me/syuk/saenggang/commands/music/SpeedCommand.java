package me.syuk.saenggang.commands.music;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class SpeedCommand implements Command {
    public static Map<Long, Double> serverMusicSpeedMap = new HashMap<>();

    @Override
    public String name() {
        return "배속";
    }

    @Override
    public Theme theme() {
        return Theme.MUSIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (message.getServer().isEmpty()) {
            message.reply("서버에서만 노래를 불러드릴 수 있어요.");
            return;
        }
        Server server = message.getServer().get();
        serverMusicSpeedMap.putIfAbsent(server.getId(), 1.0);

        Optional<ServerVoiceChannel> oChannel = api.getYourself().getConnectedVoiceChannel(message.getServer().get());
        if (oChannel.isEmpty()) {
            message.reply("노래를 부르고 있지 않아요.");
            return;
        }

        AudioPlayer player = SingingCommand.serverPlayerMap.get(server.getId());
        if (player == null) {
            message.reply("노래를 부르고 있지 않아요.");
            return;
        }

        if (args.length >= 2) {
            try {
                double speed = Double.parseDouble(args[1]);
                if (speed < 0.1 || speed > 5) {
                    message.reply("속도은 0 ~ 5 사이여야해요.");
                    return;
                }
                changeSpeed(message, server, player, speed);
            } catch (Exception e) {
                message.reply("속도은 0 ~ 5 사이여야해요.");
            }
        } else {
            message.reply("얼마나 빠르게 부를까요? (현재 속도: " + serverMusicSpeedMap.get(server.getId()) + ")");
            MessageCreated.replyCallbackMap.put(account, message1 -> {
                MessageCreated.replyCallbackMap.remove(account);
                try {
                    double speed = Double.parseDouble(message1.getContent());
                    if (speed < 0.1 || speed > 5) {
                        message.reply("속도은 0 ~ 5 사이여야해요.");
                        return true;
                    }
                    changeSpeed(message, server, player, speed);
                } catch (Exception e) {
                    message.reply("배속 명령어는 `배속 [배속]` 형식으로 사용해주세요!");
                }
                return true;
            });
        }
    }

    private void changeSpeed(Message message, Server server, AudioPlayer player, double speed) {
        player.setFilterFactory((track, format, output) -> {
            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            audioFilter.setSpeed(speed);
            return Collections.singletonList(audioFilter);
        });
        serverMusicSpeedMap.put(server.getId(), speed);
        message.reply("속도를 " + speed + "배로 설정했어요. (적용되는데 5초 정도 걸릴 수 있어요.)");
    }
}
