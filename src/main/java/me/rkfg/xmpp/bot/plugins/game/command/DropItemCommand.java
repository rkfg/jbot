package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class DropItemCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "выбросить";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            IItem item = getBackpackItem(args, player);
            player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.removeFromBackpack(item));
            player.log("Вы выбрасываете %s из рюкзака.", item.getItemDescription());
            return Optional.empty();
        } catch (NumberFormatException e) {
            return getHelp();
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Выбросить предмет из рюкзака (укажите номер предмета).");
    }

}
