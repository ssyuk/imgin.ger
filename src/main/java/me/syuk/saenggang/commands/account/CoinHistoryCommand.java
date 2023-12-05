package me.syuk.saenggang.commands.account;

import me.syuk.saenggang.Utils;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static me.syuk.saenggang.Main.api;

public class CoinHistoryCommand implements Command {
    @Override
    public String name() {
        return "코인그래프";
    }

    @Override
    public Theme theme() {
        return Theme.ACCOUNT;
    }

    @Override
    public void execute(DBManager.Account sender, String[] args, Message message) {
        User user = message.getUserAuthor().orElseThrow();
        DBManager.Account account = sender;
        if (args.length > 1) {
            String userId = args[1].replace("<@", "").replace(">", "");
            user = api.getUserById(userId).join();
            account = DBManager.getAccount(user);
        }

        int width = 800;
        int height = 600;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        List<DBManager.CoinHistory> coinHistory = DBManager.getCoinHistory(account);
        if (coinHistory.size() == 1) {
            message.reply("아직 코인 변동 내역이 기록되지 않았어요.");
            return;
        }

        List<Integer> data = new ArrayList<>();
        int lastCoin = 0;
        for (DBManager.CoinHistory history : coinHistory) {
            lastCoin += history.coin();
            data.add(lastCoin);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        drawLineGraph(g2d, data.toArray(Integer[]::new), width, height);

        message.reply(new EmbedBuilder()
                .setTitle(Utils.getUserName(user) + "님의 코인 변동 그래프")
                .setImage(image));
    }

    private static void drawLineGraph(Graphics2D g2d, Integer[] data, int width, int height) {
        // 그래프 영역 설정
        int graphWidth = width - 100;
        int graphHeight = height - 100;
        int numPoints = data.length;

        // 최대값 및 최소값 계산
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        for (int value : data) {
            if (value > maxValue) {
                maxValue = value;
            }
            if (value < minValue) {
                minValue = value;
            }
        }

        // x축에 띄울 여백 추가
        double xPadding = graphWidth / (numPoints - 1.0);

        // y축 눈금 간격 계산
        int yTickInterval = (maxValue - minValue) / 5;

        // 최대값과 최소값을 기준으로 그래프를 그릴 경로(Path2D) 생성
        Path2D path = new Path2D.Double();
        for (int i = 0; i < numPoints; i++) {
            double x = i * xPadding + 50;
            double y = height - ((data[i] - minValue) / (double) (maxValue - minValue) * graphHeight) - 50;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            // 그래프 점 그리기
            g2d.setColor(Color.BLUE);
            g2d.fillOval((int) x - 5, (int) y - 5, 10, 10);
        }

        // 꺾은선 그래프 그리기
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(path);

        // x축, y축 그리기
        g2d.setColor(Color.BLACK);
        g2d.drawLine(50, height - 50, width - 50, height - 50); // x축
        g2d.drawLine(50, 50, 50, height - 50); // y축

        // y축 눈금과 숫자 그리기
        for (int i = 0; i <= 5; i++) {
            int yValue = minValue + i * yTickInterval;
            double yCoord = height - 50 - ((double) graphHeight / 5) * i;
            g2d.drawLine(45, (int) yCoord, 50, (int) yCoord);
            g2d.drawString(Integer.toString(yValue), 40 - g2d.getFontMetrics().stringWidth(Integer.toString(yValue)), (int) yCoord + 5);
        }
    }
}
