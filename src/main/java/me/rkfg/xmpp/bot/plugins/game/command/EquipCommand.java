package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;

public class EquipCommand implements ICommandHandler, IUsesBackpack {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("надеть", "н");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            IItem item = getBackpackItem(args, player);
            item.getFittingSlot().ifPresent(slot -> player.getSlot(slot).flatMap(ISlot::getItem).ifPresent(equippedItem -> {
                if (player.enqueueEvent(new UnequipEvent(player, slot))) {
                    player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.putItemToBackpack(equippedItem));
                }
            }));
            if (player.enqueueEvent(new EquipEvent(item))) {
                player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.removeFromBackpack(item));
            }
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
