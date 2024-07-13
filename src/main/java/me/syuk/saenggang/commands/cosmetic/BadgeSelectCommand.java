package me.syuk.saenggang.commands.cosmetic;

import me.syuk.saenggang.Badge;
import me.syuk.saenggang.SelectMenuChoose;
import me.syuk.saenggang.commands.Command;
import me.syuk.saenggang.db.DBManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;

import java.util.ArrayList;
import java.util.List;

public class BadgeSelectCommand implements Command {
    @Override
    public String name() {
        return "뱃지착용";
    }

    @Override
    public Theme theme() {
        return Theme.COSMETIC;
    }

    @Override
    public void execute(DBManager.Account account, String[] args, Message message) {
        List<SelectMenuOption> options = new ArrayList<>();
        DBManager.getBadges(account).forEach(badgeId -> {
            Badge badge = Badge.getBadgeById(badgeId);
            assert badge != null;

            options.add(SelectMenuOption.create(
                    badge.getName(),
                    String.valueOf(badge.getId()),
                    badge.getName() + " 뱃지를 착용합니다."
            ));
        });
        new MessageBuilder()
                .append("착용할 뱃지를 선택해주세요.")
                .addComponents(ActionRow.of(SelectMenu.createStringMenu("badgeSelector", "어떤 뱃지를 착용하실건가요?", 1, 1, options)))
                .replyTo(message)
                .send(message.getChannel())
                .whenComplete((message1, throwable) -> {
                    if (throwable != null) throwable.printStackTrace();
                });

        SelectMenuChoose.selectMenuCallbackMap.put(account, interaction -> {
            if (interaction.getCustomId().equals("badgeSelector")) {
                if (interaction.getUser().getId() != account.userId()) {
                    interaction.createImmediateResponder()
                            .setContent("본인만 응답할 수 있습니다.").setFlags(MessageFlag.EPHEMERAL)
                            .respond();
                    return;
                }
                int badgeId = Integer.parseInt(interaction.getChosenOptions().get(0).getValue());
                DBManager.selectBadge(account, badgeId);
                interaction.createImmediateResponder()
                        .setContent(Badge.getBadgeById(badgeId) + " 뱃지를 착용했습니다.")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        });
    }
}
