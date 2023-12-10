package me.syuk.saenggang.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class VolumeCommand implements Command {
    @Override
    public String name() {
        return "볼륨";
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
                int volume = Integer.parseInt(args[1]);
                if (volume < 0 || volume > 100) {
                    message.reply("볼륨은 0 ~ 100 사이로 설정해주세요.");
                    return;
                }
                player.setVolume(volume);
                message.reply("볼륨을 " + volume + "으로 설정했어요.");
            } catch (NumberFormatException e) {
                message.reply("볼륨은 0 ~ 100 사이로 설정해주세요.");
            }
        } else {
            message.reply("볼륨을 얼마로 설정할까요? (현재 볼륨: " + player.getVolume() + ")");
            MessageCreated.replyCallbackMap.put(account, message1 -> {
                MessageCreated.replyCallbackMap.remove(account);
                try {
                    int volume = Integer.parseInt(message1.getContent());
                    if (volume < 0 || volume > 100) {
                        message.reply("볼륨은 0 ~ 100 사이로 설정해주세요.");
                        return true;
                    }
                    player.setVolume(volume);
                    message.reply("볼륨을 " + volume + "으로 설정했어요.");
                } catch (NumberFormatException e) {
                    message.reply("볼륨은 0 ~ 100 사이로 설정해주세요.");
                }
                return true;
            });
        }
    }
}
