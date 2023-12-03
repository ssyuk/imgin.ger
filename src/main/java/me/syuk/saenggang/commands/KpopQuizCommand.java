package me.syuk.saenggang.commands;

import me.syuk.saenggang.ButtonClick;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class KpopQuizCommand implements Command {
    public static QuizMusic[] MUSICS = new QuizMusic[]{
            // BTS MUSICS
            new QuizMusic("Dynamite", "BTS"),
            new QuizMusic("Life Goes On", "BTS"),
            new QuizMusic("Butter", "BTS"),
            new QuizMusic("Permission to Dance", "BTS"),

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
            new QuizMusic("Girls", "aespa"),
            new QuizMusic("Spicy", "aespa"),
            new QuizMusic("Drama", "aespa"),
            new QuizMusic("Better things" , "aespa"),
            new QuizMusic("Iconic" , "aespa"),
            new QuizMusic("Life’s too short" , "aespa"),
            new QuizMusic("Lucid dream" , "aespa"),
            new QuizMusic("Lingo" , "aespa"),
            new QuizMusic("Dreams come true" , "aespa"),
            new QuizMusic("Hold on tight" , "aespa"),
            new QuizMusic("자각몽" , "aespa"),
            new QuizMusic("aenergy" , "aespa"),
            new QuizMusic("Salty&sweet" , "aespa"),
            new QuizMusic("Welcome to my world" , "aespa"),

            // NCT DREAM MUSICS
            new QuizMusic("ISTJ", "NCT DREAM"),
            new QuizMusic("Candy", "NCT DREAM"),
            new QuizMusic("Graduation", "NCT DREAM"),
            new QuizMusic("Beatbox", "NCT DREAM"),
            new QuizMusic("버퍼링 (Glitch Mode)", "NCT DREAM"),

            // 프로미스나인 MUSICS
            new QuizMusic("Stay this way" , "프로미스나인"),
            new QuizMusic("Dm" , "프로미스나인"),
            new QuizMusic("We go" , "프로미스나인"),
            new QuizMusic("Talk&talk" , "프로미스나인"),
            new QuizMusic("#menow" , " 프로미스나인"),
            new QuizMusic("Love boom" , "프로미스나인"),
            new QuizMusic("Feel good" , "프로미스나인"),
            new QuizMusic("Escape room" , "프로미스나인"),

            
            // Red Velvet MUSICS
            new QuizMusic("Feel my rhythm" , "Red Velvet"),
            new QuizMusic("Psycho" , "Red Velvet"),
            new QuizMusic("Queendom" , "Red Velvet"),
            new QuizMusic("피카부" , "Red Velvet"),
            new QuizMusic("Badboy" , "Red Velvet"),
            new QuizMusic("Chill kill" , "Red Velvet"),
            new QuizMusic("Power up" , "Red Velvet"),
            new QuizMusic("Ice cream cake" , "Red Velvet"),
            new QuizMusic("짐살라빔" , "Red Velvet"),
            new QuizMusic("음파음파" , "Red Velvet"),
            new QuizMusic("RBB(really bad boy)" , "Red Velvet"),
            new QuizMusic("Oh boy" , "Red Velvet"),

            // EXO MUSICS
            new QuizMusic("첫 눈" , "EXO"),
            new QuizMusic("Tempo" , "EXO"),
            new QuizMusic("Love shot" , "EXO"),
            new QuizMusic("Love me right" , "EXO"),
            new QuizMusic("Lotto" , "EXO"),
            new QuizMusic("으르렁" , "EXO"),
            new QuizMusic("전야" , "EXO"),
            new QuizMusic("Power" , "EXO"),
    };

    public static QuizMusic getRandomMusic() {
        return MUSICS[new Random().nextInt(MUSICS.length)];
    }

    private static Game createGameEmbed() {
        QuizMusic quizMusic = getRandomMusic();
        List<String> artists = new ArrayList<>(List.of(quizMusic.artist()));
        List<LowLevelComponent> components = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String randomArtist = getRandomMusic().artist();
            while (artists.contains(randomArtist)) randomArtist = getRandomMusic().artist();

            components.add(Button.primary(randomArtist.toLowerCase(), randomArtist));
            artists.add(randomArtist);
        }
        components.add(Button.primary(quizMusic.artist().toLowerCase(), quizMusic.artist()));
        components.sort((o1, o2) -> {
            if (o1 instanceof Button b1 && o2 instanceof Button b2) {
                return b1.getCustomId().orElseThrow().compareTo(b2.getCustomId().orElseThrow());
            }
            return 0;
        });
        components.add(Button.danger("stop", "그만"));

        return new Game(new EmbedBuilder()
                .setTitle(quizMusic.name())
                .setDescription("누구의 노래일까요?"), ActionRow.of(components), quizMusic.artist().toLowerCase());
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
    public void execute(DBManager.Account account, String[] args, Message message) {
        ServerThreadChannel channel = Utils.createGameThread(message, "케이팝퀴즈");

        channel.sendMessage("다음 KPOP 노래 제목을 보고, 보기에서 알맞는 아티스트명의 번호를 클릭해 주세요!");
        var ref = new Object() {
            Game game = createGameEmbed();
        };
        new MessageBuilder().addEmbed(ref.game.embed()).addComponents(ref.game.component())
                .send(channel).whenComplete((message1, throwable) -> {
            throw new RuntimeException(throwable);
        });

        AtomicInteger count = new AtomicInteger();
        ButtonClick.buttonCallbackMap.put(account, interaction -> CompletableFuture.supplyAsync(() -> {
            String id = interaction.getCustomId();
            InteractionImmediateResponseBuilder response = interaction.createImmediateResponder();
            if (id.equals("stop")) {
                response.append("케이팝퀴즈를 종료합니다.").respond().whenComplete((interactionOriginalResponseUpdater, throwable) -> {
                    ButtonClick.buttonCallbackMap.remove(account);
                    channel.createUpdater().setArchivedFlag(true).update();
                    channel.removeThreadMember(Long.parseLong(account.userId()));
                });
                return true;
            }

            if (id.equals(ref.game.answer)) {
                response.append("정답입니다! 축하드려요! (연속 정답: " + count.get() + "회)");
                count.incrementAndGet();
                if (count.get() % 10 == 0) {
                    DBManager.giveCoin(account, 1);
                    response.appendNewLine();
                    response.append("연속 정답 횟수가 " + count.get() + "회가 되서" + Utils.displayCoin(1) + "을(를) 받았어요! (현재 코인: " + Utils.displayCoin(account.coin()) + ")");
                }
                ref.game = createGameEmbed();
            } else {
                count.set(0);
                response.append("틀렸어요! (연속 정답: 0회)");
                ref.game = createGameEmbed();
            }
            response.addEmbed(ref.game.embed());
            response.addComponents(ref.game.component());
            response.respond();
            return true;
        }).join());
    }

    public record QuizMusic(String name, String artist) {
    }

    public record Game(EmbedBuilder embed, HighLevelComponent component, String answer) {
    }
}
