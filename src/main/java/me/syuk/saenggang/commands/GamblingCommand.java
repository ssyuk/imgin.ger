package me.syuk.saenggang.commands;

import me.syuk.saenggang.MessageCreated;
import me.syuk.saenggang.db.Account;
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
    public void execute(Account account, String[] args, Message message) {
        int coins = Integer.parseInt(args[1]);
        if (account.coin() < coins) {
            message.reply("코인이 부족해요!");
            return;
        }


        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Collections.shuffle(numbers);

        message.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
        MessageCreated.replyListener.put(account, replyMessage -> {
            String content = replyMessage.getContent();
            int number;
            try {
                number = Integer.parseInt(content);
            } catch (NumberFormatException e) {
                replyMessage.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
                return;
            }
            if (1 > number || number > 10) {
                replyMessage.reply("\uD83C\uDFB0 1~10 사이의 숫자를 입력해주세요!");
                return;
            }

            int index = numbers.indexOf(number);
            switch (index) {
                case 0 -> {
                    replyMessage.reply("\uD83C\uDF89 축하해요! `\uD83E\uDE99" + coins * 2 + "`을 얻었어요! (2배)");
                    DBManager.giveCoin(account, coins);
                }
                case 1, 2 -> {
                    replyMessage.reply("\uD83C\uDF89 축하해요! `\uD83E\uDE99" + (int) (coins * 1.5) + "`을 얻었어요! (1.5배)");
                    DBManager.giveCoin(account, (int) (coins * .5));
                }
                case 3, 4, 5 -> {
                    replyMessage.reply("코인을 그대로 돌려받았어요! (1배)");
                }
                case 6, 7 -> {
                    replyMessage.reply("\uD83D\uDC94 `\uD83E\uDE99" + (int) (coins * .3) + "`을 잃었어요ㅠ (0.7배)");
                    DBManager.giveCoin(account, (int) -(coins * .3));
                }
                case 8 -> {
                    replyMessage.reply("\uD83D\uDC94  `\uD83E\uDE99" + (int) (coins * .6) + "`을 잃었어요ㅠ (0.4배)");
                    DBManager.giveCoin(account, (int) -(coins * .6));
                }
                case 9 -> {
                    replyMessage.reply("\uD83C\uDF29 코인을 전부 다 잃었어요. (0배)");
                    DBManager.giveCoin(account, -coins);
                }
            }
            MessageCreated.replyListener.remove(account);
        });
    }
}
