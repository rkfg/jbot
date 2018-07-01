package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;

public class UnequipCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("снять", "сн");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        args.findFirst().map(a -> {
            switch (a) {
            case "о":
                return WEAPON_SLOT;
            case "б":
                return ARMOR_SLOT;
            default:
                return null;
            }
        }).ifPresent(slot -> player.getSlot(slot).flatMap(ISlot::getItem).ifPresent(i -> {
            if (player.enqueueEvent(new UnequipEvent(player, slot))) {
                player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.putItemToBackpack(i));
            }
        }));
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("освободить слот (о — оружие, б — броня)");
    }

}
