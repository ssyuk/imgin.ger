package me.syuk.saenggang.commands;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GamblingCommand implements Command {
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

        int coins = Integer.parseInt(args[1]);
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
            switch (index) {
                case 0, 1 -> {
                    replyMessage.reply("\uD83C\uDF89 축하해요! " + Utils.displayCoin(coins) + "을 얻었어요! (2배)");
                    DBManager.giveCoin(account, coins);
                }
                case 2, 3 -> {
                    replyMessage.reply("\uD83C\uDF89 축하해요! " + Utils.displayCoin((int) (coins * 0.5)) + "을 얻었어요! (1.5배)");
                    DBManager.giveCoin(account, (int) (coins * .5));
                }
                case 4, 5, 6 -> replyMessage.reply("코인을 그대로 돌려받았어요! (1배)");
                case 7, 8 -> {
                    replyMessage.reply("\uD83D\uDC94 " + Utils.displayCoin((int) (coins * .3)) + "을 잃었어요ㅠ (0.7배)");
                    DBManager.giveCoin(account, (int) -(coins * .3));
                }
                case 9 -> {
                    replyMessage.reply("\uD83C\uDF29 " + Utils.displayCoin((int) (coins * .6)) + "을 잃었어요ㅠ (0.4배)");
                    DBManager.giveCoin(account, (int) -(coins * .6));
                }
            }
            MessageCreated.replyCallbackMap.remove(account);
            return true;
        });
    }
}
