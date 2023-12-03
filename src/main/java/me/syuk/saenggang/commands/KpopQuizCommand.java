package me.syuk.saenggang.commands;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class KpopQuizCommand implements Command {
    public static QuizMusic[] MUSICS = new QuizMusic[]{
            // BTS MUSICS
            new QuizMusic("Dynamite", "방탄소년단"),
            new QuizMusic("Life Goes On", "방탄소년단"),
            new QuizMusic("Butter", "방탄소년단"),
            new QuizMusic("Permission to Dance", "방탄소년단"),

            // LE SSERAFIM MUSICS
            new QuizMusic("FEARLESS", "LE SSERAFIM"),
            new QuizMusic("ANTIFRAGILE", "LE SSERAFIM"),
            new QuizMusic("UNFORGIVEN", "LE SSERAFIM"),
            new QuizMusic("이브, 프시케 그리고 푸른 수염의 아내", "LE SSERAFIM"),
            new QuizMusic("UNFORGIVEN", "LE SSERAFIM"),
            new QuizMusic("Perfect Night", "LE SSERAFIM"),

            // NewJeans MUSICS
            new QuizMusic("Attention", "NewJeans"),
            new QuizMusic("Hype Boy", "NewJeans"),
            new QuizMusic("Cookie", "NewJeans"),
            new QuizMusic("Hurt", "NewJeans"),
            new QuizMusic("Ditto", "NewJeans"),
            new QuizMusic("OMG", "NewJeans"),
            new QuizMusic("New Jeans", "NewJeans"),
            new QuizMusic("Super Shy", "NewJeans"),
            new QuizMusic("ETA", "NewJeans"),
            new QuizMusic("Cool With You", "NewJeans"),
            new QuizMusic("Get Up", "NewJeans"),

            // aespa MUSICS
            new QuizMusic("Black Mamba", "aespa"),
            new QuizMusic("Next Level", "aespa"),
            new QuizMusic("Savage", "aespa"),
            new QuizMusic("Dreams Come True", "aespa"),
            new QuizMusic("도깨비 불 (Illusion)", "aespa"),
            new QuizMusic("Life's Too Short", "aespa"),
            new QuizMusic("Girls", "aespa"),
            new QuizMusic("Spicy", "aespa"),
            new QuizMusic("Drama", "aespa"),

            // NCT DREAM MUSICS
            new QuizMusic("ISTJ", "NCT DREAM"),
            new QuizMusic("Candy", "NCT DREAM"),
            new QuizMusic("Graduation", "NCT DREAM"),
            new QuizMusic("Beatbox", "NCT DREAM"),
            new QuizMusic("버퍼링 (Glitch Mode)", "NCT DREAM"),
    };

    public static QuizMusic getRandomMusic() {
        return MUSICS[new Random().nextInt(MUSICS.length)];
    }

    private static Game createGameEmbed() {
        QuizMusic quizMusic = getRandomMusic();
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(quizMusic.name())
                .setDescription("누구의 노래일까요?")
                .setFooter("그만하고 싶으시면 `그만`이라고 말해주세요!");
        int answer = new Random().nextInt(4) + 1;
        List<String> artists = new ArrayList<>(List.of(quizMusic.artist()));
        for (int i = 1; i <= 5; i++) {
            if (i == answer) builder.addInlineField(String.valueOf(i), quizMusic.artist());
            else {
                String randomArtist = getRandomMusic().artist();
                while (artists.contains(randomArtist)) randomArtist = getRandomMusic().artist();

                builder.addInlineField(String.valueOf(i), randomArtist);
                artists.add(randomArtist);
            }
        }
        return new Game(builder, answer);
    }

    @Override
    public String name() {
        return "케이팝퀴즈";
    }

    @Override
    public Theme theme() {
        return Theme.GAME;
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        ServerThreadChannel channel = Utils.createGameThread(message, "케이팝퀴즈");

        channel.sendMessage("다음 KPOP 노래 제목을 보고, 보기에서 알맞는 아티스트명의 번호를 입력해 주세요!");
        var ref = new Object() {
            Game game = createGameEmbed();
        };
        channel.sendMessage(ref.game.builder());

        AtomicInteger count = new AtomicInteger();
        MessageCreated.replyListener.put(account, replyMessage -> {
            if (replyMessage.getChannel().getId() != channel.getId()) return false;

            return CompletableFuture.supplyAsync(() -> {
                if (replyMessage.getContent().equals("그만")) {
                    channel.sendMessage("케이팝퀴즈를 종료합니다.");
                    MessageCreated.replyListener.remove(account);
                    channel.createUpdater().setArchivedFlag(true).update();
                    return true;
                }

                int answer = ref.game.answer;
                try {
                    int input = Integer.parseInt(replyMessage.getContent());
                    if (input == answer) {
                        channel.sendMessage("정답입니다! 축하드려요!");
                        count.incrementAndGet();
                        if (count.get() % 10 == 0) {
                            account.giveCoin(replyMessage.getChannel(), 1, "연속 정답 횟수가 " + count.get() + "회가 되서");
                        }
                        ref.game = createGameEmbed();
                        channel.sendMessage(ref.game.builder());
                    } else {
                        count.set(0);
                        channel.sendMessage("틀렸어요! 연속 정답 횟수가 초기화되었습니다. 다시 시도해보세요!\n" +
                                "그만하고 싶으시면 `그만`이라고 말해주세요!");
                    }
                } catch (NumberFormatException e) {
                    channel.sendMessage("숫자로 입력해주세요!");
                }
                return true;
            }).join();
        });
    }

    public record QuizMusic(String name, String artist) {
    }

    public record Game(EmbedBuilder builder, int answer) {
    }
}
