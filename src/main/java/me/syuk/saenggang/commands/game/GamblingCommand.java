package me.syuk.saenggang.commands.game;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GamblingCommand implements Command {
    public static Map<DBManager.Account, LocalDateTime> lastMessageTime = new HashMap<>();

    @Override
    public String name() {
        return "도박";
    }

    @Override
    public Theme theme() {
        return Theme.GAME;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        if (args.length != 2 || !args[1].matches("\\d+")) {
            message.reply("도박 명령어는 `도박 [걸 코인]` 형식으로 사용해주세요!");
            return;
        }

        if (lastMessageTime.containsKey(account)) {
            LocalDateTime lastTime = lastMessageTime.get(account);
            long secondsBetween = ChronoUnit.SECONDS.between(lastTime, LocalDateTime.now());
            if (secondsBetween < 1) return;
        }
        lastMessageTime.put(account, LocalDateTime.now());

        int coins = Integer.parseInt(args[1]);
        if (coins > 300) {
            message.reply("최대 " + Utils.displayCoin(300) + "까지 걸 수 있어요!");
            return;
        }
        if (account.coin() < coins) {
            message.reply("코인이 부족해요!");
            return;
        }


        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Collections.shuffle(numbers);

        message.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
        MessageCreated.replyCallbackMap.put(account, replyMessage -> {
            String content = replyMessage.getContent();
            int number;
            try {
                number = Integer.parseInt(content);
            } catch (NumberFormatException e) {
                replyMessage.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
                return true;
            }
            if (1 > number || number > 10) {
                replyMessage.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
                return true;
            }

            int index = numbers.indexOf(number);
            StringBuilder reply = new StringBuilder();
            switch (index) {
                case 0 -> {
                    reply.append(":cloud_lightning: ").append(Utils.displayCoin((int) (coins * .6))).append("을 잃었어요ㅠ (0.4배)");
                    DBManager.giveCoin(account, (int) -(coins * .6));
                }
                case 1,2  -> {
                    reply.append(":broken_heart: ").append(Utils.displayCoin((int) (coins * .5))).append("을 잃었어요ㅠ (0.5배)");
                    DBManager.giveCoin(account, (int) -(coins * .5));
                }
                case 3,4 -> {
                    reply.append(":cry: ").append(Utils.displayCoin((int) (coins * .3))).append("을 잃었어요ㅠ (0.7배)");
                    DBManager.giveCoin(account, (int) -(coins * .3));
                }
                case 5,6 -> {
                    reply.append(":v: 축하해요! ").append(Utils.displayCoin((int) (coins * .2))).append("을 얻었어요! (1.2배)");
                    DBManager.giveCoin(account, (int) (coins * .2));
                }
                case 7 -> {
                    reply.append(":laughing: 축하해요! ").append(Utils.displayCoin((int) (coins * .5))).append("을 얻었어요! (1.5배)");
                    DBManager.giveCoin(account, (int) (coins * .5));
                }
                case 8 -> {
                    reply.append(":tada: 축하해요! ").append(Utils.displayCoin((int) (coins * .6))).append("을 얻었어요! (1.6배)");
                    DBManager.giveCoin(account, (int) (coins * .6));
                }
                case 9 -> {
                    reply.append(":sparkles: 축하해요! ").append(Utils.displayCoin((int) (coins * .7))).append("을 얻었어요! (1.7배)");
                    DBManager.giveCoin(account, (int) (coins * .7));
                }
            }
            reply.append("\n").append("현재 코인: ").append(Utils.displayCoin(account.coin()));
            replyMessage.reply(reply.toString());
            MessageCreated.replyCallbackMap.remove(account);
            return true;
        });
    }
}
