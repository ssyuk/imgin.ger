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
            StringBuilder reply = new StringBuilder();
            switch (index) {
                case 0 -> {
                    reply.append("\uD83C\uDF89 축하해요! ").append(Utils.displayCoin(coins)).append("을 얻었어요! (2배)");
                    DBManager.giveCoin(account, coins);
                }
                case 1, 2 -> {
                    reply.append("\uD83C\uDF89 축하해요! ").append(Utils.displayCoin((int) (coins * 0.5))).append("을 얻었어요! (1.5배)");
                    DBManager.giveCoin(account, (int) (coins * .5));
                }
                case 3, 4 -> {
                    reply.append("\uD83C\uDF89 축하해요! ").append(Utils.displayCoin((int) (coins * 0.2))).append("을 얻었어요! (1.2배)");
                    DBManager.giveCoin(account, (int) (coins * .2));
                }
                case 5, 6 -> reply.append("코인을 그대로 돌려받았어요! (1배)");
                case 7, 8 -> {
                    reply.append("\uD83D\uDC94 ").append(Utils.displayCoin((int) (coins * .4))).append("을 잃었어요ㅠ (0.6배)");
                    DBManager.giveCoin(account, (int) -(coins * .4));
                }
                case 9 -> {
                    reply.append("\uD83C\uDF29 ").append(Utils.displayCoin((int) (coins * .8))).append("을 잃었어요ㅠ (0.2배)");
                    DBManager.giveCoin(account, (int) -(coins * .8));
                }
            }
            reply.append("\n").append("현재 코인: ").append(Utils.displayCoin(account.coin()));
            replyMessage.reply(reply.toString());
            MessageCreated.replyCallbackMap.remove(account);
            return true;
        });
    }
}
