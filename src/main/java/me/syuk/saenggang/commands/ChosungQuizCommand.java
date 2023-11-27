package me.syuk.saenggang.commands;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.db.Account;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerThreadChannelBuilder;
import org.javacord.api.entity.message.Message;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChosungQuizCommand implements Command {
    @Override
    public String name() {
        return "초성퀴즈";
    }

    String getInitialSound(String text) {
        String[] chs = {
                "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ",
                "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
                "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ",
                "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        };

        AtomicReference<String> result = new AtomicReference<>("");

        text.chars().forEach(value -> {
            if (value >= 0xAC00) {
                int uniVal = value - 0xAC00;
                int cho = ((uniVal - (uniVal % 28)) / 28) / 21;

                result.set(result.get() + chs[cho]);
            }
        });

        return result.get();
    }

    @Override
    public void execute(Account account, String[] args, Message message) {
        ServerThreadChannel channel = new ServerThreadChannelBuilder(message, "생강이와 초성퀴즈")
                .create().join();

        AtomicReference<ChosungQuiz.QuizWord> word = new AtomicReference<>(ChosungQuiz.getRandomWord());
        channel.sendMessage("초성퀴즈 내드릴게요! 무슨 단어일까요?\n" +
                "# " + getInitialSound(word.get().word()) + "\n" +
                "힌트: __**" + word.get().theme() + "**__\n");

        AtomicInteger count = new AtomicInteger();
        MessageCreated.replyListener.put(account, replyMessage -> {
            if (replyMessage.getChannel().getId() != channel.getId()) return;

            CompletableFuture.runAsync(() -> {
                if (replyMessage.getContent().equals("그만")) {
                    channel.sendMessage("초성퀴즈를 종료합니다.");
                    MessageCreated.replyListener.remove(account);
                    channel.removeThreadMember(Long.parseLong(account.userId()));
                    channel.leaveThread();
                    channel.delete();
                    return;
                }

                if (replyMessage.getContent().equals(word.get().word())) {
                    channel.sendMessage("정답입니다! 축하드려요!");
                    count.incrementAndGet();
                    if (count.get() % 100 == 0) {
                        account.giveCoin(replyMessage.getChannel(), 5, count.get() + "회 연속 정답을 맞춰서");
                    } else if (count.get() % 5 == 0) {
                        account.giveCoin(replyMessage.getChannel(), 1, "연속 정답 횟수가 " + count.get() + "회가 되서");
                    }
                    word.set(ChosungQuiz.getRandomWord());
                    channel.sendMessage("다음 문제를 내드릴게요! (연속 정답: " + count.get() + "회)\n" +
                            "# " + getInitialSound(word.get().word()) + "\n" +
                            "힌트: __**" + word.get().theme() + "**__\n" +
                            "그만하고 싶으시면 `그만`이라고 말해주세요!");
                } else {
                    count.set(0);
                    channel.sendMessage("틀렸어요! 연속 정답 횟수가 초기화되었습니다. 다시 시도해보세요!\n" +
                            "그만하고 싶으시면 `그만`이라고 말해주세요!");
                }
            });
        });
    }

    public static class ChosungQuiz {
        public static String[] ANIMAL = new String[]{
                "강아지", "고양이", "원숭이", "코끼리", "사자", "호랑이", "코알라", "판다", "기린", "캥거루",
                "앵무새", "카멜레온", "다람쥐", "하마", "펭귄", "돌고래", "햄스터", "침팬지", "물범",
                "악어", "오리", "비둘기", "거북이", "뱀", "코브라", "말", "쥐", "닭", "사슴",
        };
        public static String[] FRUITS = new String[]{
                "사과", "바나나", "체리", "레몬", "오렌지", "포도", "딸기", "복숭아", "수박", "키위",
                "멜론", "파인애플", "블루베리", "망고", "토마토", "자두", "복분자", "감", "거봉", "라임", "배",
        };

        public static Map<String, String[]> WORD_LISTS = Map.of(
                "동물", ANIMAL,
                "과일", FRUITS
        );

        public static QuizWord getRandomWord() {
            int wordListAt = new Random().nextInt(WORD_LISTS.size());
            String[] wordList = WORD_LISTS.values().toArray(String[][]::new)[wordListAt];
            String word = wordList[new Random().nextInt(wordList.length)];
            return new QuizWord(word, WORD_LISTS.keySet().toArray(String[]::new)[wordListAt]);
        }

        public record QuizWord(String word, String theme) {
        }
    }
}
