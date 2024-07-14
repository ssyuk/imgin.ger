package me.syuk.saenggang.ai.functions.music;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import me.syuk.saenggang.ButtonClick;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import me.syuk.saenggang.music.LavaplayerAudioSource;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.syuk.saenggang.Main.api;

public class SingingFunction implements AIFunction {
    public static Map<Long, AudioPlayerManager> serverPlayerManagerMap = new HashMap<>();
    public static Map<Long, AudioPlayer> serverPlayerMap = new HashMap<>();
    public static Map<Long, AudioConnection> serverConnectionMap = new HashMap<>();
    public static Map<Long, List<AudioTrack>> serverPlaylistMap = new HashMap<>();

    @Override
    public String name() {
        return "music_start_singing";
    }

    @Override
    public String description() {
        return "생강이가 노래를 부릅니다. (혹은 노래를 재생합니다)";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("source", "string", "재생할 노래입니다. (제목 or 유튜브 링크)", true)
        );
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message message) {
        JsonObject response = new JsonObject();

        TextChannel textChannel = message.getChannel();
        if (message.getServer().isEmpty()) {
            response.addProperty("error", "서버에서만 노래기능 사용 가능");
            return response;
        }
        Server server = message.getServer().get();
        User user = message.getUserAuthor().orElseThrow();
        Optional<ServerVoiceChannel> oChannel = server.getConnectedVoiceChannel(user);
        if (oChannel.isEmpty()) {
            response.addProperty("error", "사용자가 음성채널에 접속해있지 않음");
            return response;
        }

        serverPlaylistMap.putIfAbsent(server.getId(), new ArrayList<>());
        ServerVoiceChannel channel = oChannel.get();
        String source = args.get("source").startsWith("http") ? args.get("source") : "ytsearch:" + args.get("source");
        if (serverConnectionMap.containsKey(server.getId())) {
            addToPlaylist(account, server.getId(), source, message);
            response.addProperty("status", "플레이리스트에 추가됨");
            return null;
        }

        channel.connect().thenAccept(audioConnection -> {
            serverConnectionMap.put(server.getId(), audioConnection);

            AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
            playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
            playerManager.getConfiguration().setFilterHotSwapEnabled(true);

            AudioPlayer player = playerManager.createPlayer();

            audioConnection.setAudioSource(new LavaplayerAudioSource(api, player));

            player.addListener(audioEvent -> {
                if (audioEvent instanceof TrackEndEvent) {
                    List<AudioTrack> playlist = serverPlaylistMap.get(server.getId());
                    if (playlist.isEmpty()) {
                        audioConnection.close();
                        serverPlayerMap.remove(server.getId());
                        serverConnectionMap.remove(server.getId());
                        serverPlaylistMap.remove(server.getId());
                        textChannel.sendMessage("더이상 불러드릴 노래가 없어요. 음성채널을 나갈게요.");
                    } else {
                        AudioTrack nextSong = playlist.get(0);
                        playlist.remove(0);
                        serverPlaylistMap.put(server.getId(), playlist);
                        textChannel.sendMessage("`" + nextSong.getInfo().title + "`을(를) 불러드릴게요!");
                        player.playTrack(nextSong);
                    }
                }
            });
            serverPlayerManagerMap.put(server.getId(), playerManager);
            serverPlayerMap.put(server.getId(), player);

            addToPlaylist(account, server.getId(), source, message);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        return null;
    }

    private void addToPlaylist(DBManager.Account requester, long serverId, String source, Message message) {
        serverPlayerManagerMap.get(serverId).loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                List<AudioTrack> playlist = serverPlaylistMap.get(serverId);
                if (playlist.isEmpty() && serverPlayerMap.get(serverId).getPlayingTrack() == null) {
                    message.getChannel().sendMessage("`" + track.getInfo().title + "`을(를) 불러드릴게요!");
                    serverPlayerMap.get(serverId).playTrack(track);
                    return;
                }

                playlist.add(track);
                serverPlaylistMap.put(serverId, playlist);
                message.reply("`" + track.getInfo().title + "`을(를) 플레이리스트에 추가했어요!");
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                MessageBuilder builder = new MessageBuilder();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("어떤 곡을 재생할까요?");

                List<LowLevelComponent> components = new ArrayList<>();
                components.add(Button.primary("all", "모두 재생"));

                List<AudioTrack> tracks = audioPlaylist.getTracks().subList(0, 4);
                for (int i = 0; i < tracks.size(); i++) {
                    embed.addField("**" + (i + 1) + "**. " + tracks.get(i).getInfo().title, "");
                    components.add(Button.secondary(String.valueOf(i), (i + 1) + "번"));
                }
                builder.addEmbed(embed);
                builder.addComponents(ActionRow.of(components));

                System.out.println(tracks);
                Message requestMessage = builder.send(message.getChannel()).join();

                ButtonClick.buttonCallbackMap.put(requester, interaction -> CompletableFuture.supplyAsync(() -> {
                    String btnId = interaction.getCustomId();
                    List<AudioTrack> playlist = serverPlaylistMap.get(serverId);
                    StringBuilder addedSong = new StringBuilder();
                    if (btnId.equals("all")) {
                        playlist.addAll(tracks);
                        for (AudioTrack track : tracks) {
                            addedSong.append(track.getInfo().title).append(", ");
                        }
                    } else {
                        int index = Integer.parseInt(btnId);
                        playlist.add(tracks.get(index));
                        addedSong = new StringBuilder(tracks.get(index).getInfo().title + ", ");
                    }
                    serverPlaylistMap.put(serverId, playlist);
                    requestMessage.delete().join();

                    if (serverPlayerMap.get(serverId).getPlayingTrack() == null) {
                        AudioTrack nextSong = playlist.get(0);
                        playlist.remove(0);
                        serverPlaylistMap.put(serverId, playlist);
                        message.getChannel().sendMessage("`" + nextSong.getInfo().title + "`을(를) 불러드릴게요!");
                        interaction.createImmediateResponder().respond();
                        serverPlayerMap.get(serverId).playTrack(nextSong);
                    } else {
                        interaction.createImmediateResponder().setContent("`" + addedSong.substring(0, addedSong.length() - 2) + "`을(를) 플레이리스트에 추가했어요!").respond();
                    }
                    ButtonClick.buttonCallbackMap.remove(requester);
                    return true;
                }).join());
            }

            @Override
            public void noMatches() {
                message.reply("노래를 찾을 수 없어요. (올바른 유튜브 링크를 입력해주세요. / 입력된 링크: " + source + ")");
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                throwable.printStackTrace();
                message.reply("노래를 불러오는데 실패했어요. (올바른 유튜브 링크를 입력해주세요. / 입력된 링크: " + source + ")");
            }
        });
    }
}
