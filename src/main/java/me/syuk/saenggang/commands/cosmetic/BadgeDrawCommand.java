package me.syuk.saenggang.commands.cosmetic;

import me.syuk.saenggang.ButtonClick;
import me.syuk.saenggang.Utils;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

public class BadgeDrawCommand implements Command {
    @Override
    public String name() {
        return "뱃지뽑기";
    }

    @Override
    public Theme theme() {
        return Theme.COSMETIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        new MessageBuilder()
                .append("뱃지를 뽑으시겠습니까? (가격: " + Utils.displayCoin(100) + ")\n뱃지는 랜덤으로 지급됩니다. (뱃지별로 확률은 다릅니다.)\n뽑은 뱃지가 이미 가지고 계신 뱃지라면 " + Utils.displayCoin(50) + "을 돌려드립니다.")
                .addComponents(ActionRow.of(
                        Button.primary("badgeDraw", "뽑기"),
                        Button.danger("badgeDrawCancel", "취소")
                ))
                .replyTo(message)
                .send(message.getChannel())
                .whenComplete((message1, throwable) -> {
                    if (throwable != null) throwable.printStackTrace();
                });
        ButtonClick.buttonCallbackMap.put(account, interaction -> {
            InteractionImmediateResponseBuilder response = interaction.createImmediateResponder();
            if (interaction.getCustomId().equals("badgeDraw")) {
                if (!interaction.getUser().getIdAsString().equals(account.userId())) {
                    response.setContent("본인만 응답할 수 있습니다.").respond();
                    return;
                }

                if (account.coin() < 100) {
                    response.setContent("코인이 부족합니다.").respond();
                    return;
                }

                int badgeId = DBManager.drawBadge(account);
                account.giveCoin(message.getChannel(), -100, "뱃지뽑기에");
                response.append("뽑은 뱃지: " + Utils.getBadge(badgeId));
                if (!DBManager.addBadge(account, badgeId)) {
                    response.append("\n이미 가지고 계신 뱃지라서 " + Utils.displayCoin(50) + "을 돌려드렸어요!");
                    DBManager.giveCoin(account, 50);
                }
                response.respond();
            } else if (interaction.getCustomId().equals("badgeDrawCancel")) {
                if (!interaction.getUser().getIdAsString().equals(account.userId())) {
                    response.setContent("본인만 응답할 수 있습니다.").respond();
                    return;
                }

                response.setContent("뱃지뽑기를 취소했습니다.").respond();
            }
        });
    }
}
