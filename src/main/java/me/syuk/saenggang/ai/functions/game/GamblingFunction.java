package me.syuk.saenggang.ai.functions.game;

import com.google.gson.JsonObject;
import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.ai.AIFunction;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GamblingFunction implements AIFunction {
    public static Map<DBManager.Account, LocalDateTime> lastMessageTime = new HashMap<>();

    @Override
    public String name() {
        return "gambling";
    }

    @Override
    public String description() {
        return "If you bet the desired number of coins and guess a number between 0 and 9, you will win n times the number of coins you bet!";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(
                new Parameter("coin", "integer", "Number of coins to bet", true)
        );
    }

    @Override
    public JsonObject execute(DBManager.Account account, Map<String, String> args, Message requestMessage) {
        if (lastMessageTime.containsKey(account)) {
            LocalDateTime lastTime = lastMessageTime.get(account);
            long secondsBetween = ChronoUnit.SECONDS.between(lastTime, LocalDateTime.now());
            if (secondsBetween < 1) return null;
        }
        lastMessageTime.put(account, LocalDateTime.now());

        int coins = Integer.parseInt(args.get("coin"));
        if (coins > 300) {
            requestMessage.reply("300코인 이상은 걸 수 없어요!");
            return null;
        }
        if (account.coin() < coins) {
            requestMessage.reply("코인이 부족해요! (현재 코인: " + Utils.displayCoin(account.coin()) + ")");
            return null;
        }


        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        Collections.shuffle(numbers);

        requestMessage.reply("0~9 사이의 숫자를 입력해주세요!");
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
                    reply.append(":cloud_lightning: ").append(Utils.displayCoin((int) (coins * .5))).append("을 잃었어요ㅠ (0.5배)");
                    DBManager.giveCoin(account, (int) -(coins * .5));
                }
                case 1,2  -> {
                    reply.append(":broken_heart: ").append(Utils.displayCoin((int) (coins * .35))).append("을 잃었어요ㅠ (0.65배)");
                    DBManager.giveCoin(account, (int) -(coins * .35));
                }
                case 3,4 -> {
                    reply.append(":broken_heart: ").append(Utils.displayCoin((int) (coins * .1))).append("을 잃었어요ㅠ (0.90배)");
                    DBManager.giveCoin(account, (int) -(coins * .1));
                }
                case 5,6 -> {
                    reply.append(":v: 축하해요! ").append(Utils.displayCoin((int) (coins * .35))).append("을 얻었어요! (1.35배)");
                    DBManager.giveCoin(account, (int) (coins * .35));
                }
                case 7 -> {
                    reply.append(":laughing: 축하해요! ").append(Utils.displayCoin((int) (coins * .65))).append("을 얻었어요! (1.65배)");
                    DBManager.giveCoin(account, (int) (coins * .65));
                }
                case 8 -> {
                    reply.append(":tada: 축하해요! ").append(Utils.displayCoin((int) (coins * .75))).append("을 얻었어요! (1.75배)");
                    DBManager.giveCoin(account, (int) (coins * .75));
                }
                case 9 -> {
                    reply.append(":sparkles: 축하해요! ").append(Utils.displayCoin((int) (coins * .9))).append("을 얻었어요! (1.9배)");
                    DBManager.giveCoin(account, (int) (coins * 0.9));
                }
            }
            reply.append("\n").append("현재 코인: ").append(Utils.displayCoin(account.coin()));
            replyMessage.reply(reply.toString());
            MessageCreated.replyCallbackMap.remove(account);
            return true;
        });
        return null;
    }
}
