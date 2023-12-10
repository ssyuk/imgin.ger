package me.syuk.saenggang.commands.music;

import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import java.util.Optional;

import static me.syuk.saenggang.Main.api;

public class StopSingingCommand implements Command {
    @Override
    public String name() {
        return "그만불러줘";
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

        Optional<ServerVoiceChannel> oChannel = api.getYourself().getConnectedVoiceChannel(server);
        if (oChannel.isEmpty()) {
            message.reply("노래를 부르고 있지 않아요.");
            return;
        }

        message.reply("그만 불러드릴게요!");
        oChannel.get().disconnect();
        SingingCommand.serverPlayerMap.remove(server.getId());
        SingingCommand.serverConnectionMap.remove(server.getId());
    }
}
