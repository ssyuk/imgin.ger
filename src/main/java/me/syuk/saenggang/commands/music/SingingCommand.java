package me.syuk.saenggang.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.music.LavaplayerAudioSource;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.*;

import static me.syuk.saenggang.Main.api;

public class SingingCommand implements Command {
    public static Map<Long, AudioPlayer> serverPlayerMap = new HashMap<>();
    public static Map<Long, AudioConnection> serverConnectionMap = new HashMap<>();
    public static Map<Long, List<String>> serverPlaylistMap = new HashMap<>();

    @Override
    public String name() {
        return "노래불러줘";
    }

    @Override
    public Theme theme() {
        return Theme.MUSIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (args.length >= 2) {
            singing(args[1], message);
        } else {
            message.reply("어떤 노래를 불러드릴까요? (유튜브 링크를 입력해주세요.)");
            MessageCreated.replyCallbackMap.put(account, message1 -> {
                MessageCreated.replyCallbackMap.remove(account);
                singing(message1.getContent(), message);
                return true;
            });
        }
    }

    private void singing(String songName, Message message) {
        TextChannel textChannel = message.getChannel();
        if (message.getServer().isEmpty()) {
            message.reply("서버에서만 노래를 불러드릴 수 있어요.");
            return;
        }
        Server server = message.getServer().get();
        User user = message.getUserAuthor().orElseThrow();
        Optional<ServerVoiceChannel> oChannel = server.getConnectedVoiceChannel(user);
        if (oChannel.isEmpty()) {
            message.reply("먼저 음성 채널에 들어가주세요.");
            return;
        }

        ServerVoiceChannel channel = oChannel.get();
        if (serverConnectionMap.containsKey(server.getId())) {
            serverPlaylistMap.putIfAbsent(server.getId(), new ArrayList<>());
            List<String> playlist = serverPlaylistMap.get(server.getId());
            playlist.add(songName);
            serverPlaylistMap.put(server.getId(), playlist);
            message.reply("`" + songName + "`를 플레이리스트에 추가했어요!");
            return;
        }

        channel.connect().thenAccept(audioConnection -> {
            serverConnectionMap.put(server.getId(), audioConnection);
            message.reply("`" + songName + "`을(를) 불러드릴게요!");

            AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
            playerManager.registerSourceManager(new YoutubeAudioSourceManager());

            AudioPlayer player = playerManager.createPlayer();

            player.addListener(audioEvent -> {
                if (audioEvent instanceof TrackEndEvent) {
                    List<String> playlist = serverPlaylistMap.get(server.getId());
                    if (playlist.isEmpty()) {
                        audioConnection.close();
                        serverPlayerMap.remove(server.getId());
                        serverConnectionMap.remove(server.getId());
                        serverPlaylistMap.remove(server.getId());
                        textChannel.sendMessage("더이상 불러드릴 노래가 없어요. 음성채널을 나갈게요.");
                    } else {
                        String nextSong = playlist.get(0);
                        playlist.remove(0);
                        serverPlaylistMap.put(server.getId(), playlist);
                        textChannel.sendMessage("다 불러드렸어요. 다음으로는 `" + nextSong + "`을(를) 불러드릴게요!");
                        playMusic(player, audioConnection, playerManager, nextSong, message);
                    }
                }
            });

            serverPlayerMap.put(server.getId(), player);

            playMusic(player, audioConnection, playerManager, songName, message);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private void playMusic(AudioPlayer player, AudioConnection audioConnection, AudioPlayerManager playerManager, String songName, Message message) {
        AudioSource source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);

        playerManager.loadItem(songName, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    player.playTrack(track);
                }
            }

            @Override
            public void noMatches() {
                message.reply("노래를 찾을 수 없어요. (올바른 유튜브 링크를 입력해주세요.)");
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                message.reply("노래를 불러오는데 실패했어요. (올바른 유튜브 링크를 입력해주세요.)");
            }
        });
    }
}
