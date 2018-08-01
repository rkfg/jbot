package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class EquipCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "надеть";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            IItem item = getBackpackItem(args, player);
            player.enqueueEquipItem(item);
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Вставить предмет в подходящий слот (укажите номер предмета из рюкзака).");
    }

}
