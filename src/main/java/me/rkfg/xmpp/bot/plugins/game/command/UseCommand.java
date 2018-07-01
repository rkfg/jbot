package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.UseEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class UseCommand implements ICommandHandler, IUsesBackpack {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("использовать", "исп");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            IItem item = getBackpackItem(args, player);
            final UseEvent useEvent = new UseEvent();
            useEvent.setTarget(player);
            if (item.enqueueEvent(useEvent)) {
                player.log("Предмет %s использован!", unboxString(item.getDescription()));
                item.onUse();
            } else {
                player.log("Предмет %s не удалось использовать.", unboxString(item.getDescription()));
            }
            return Optional.empty();
        } catch (NumberFormatException e) {
            return getHelp();
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Попробовать использовать предмет (укажите номер в рюкзаке)");
    }

}
