package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class UnequipCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("снять");
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
        }).ifPresent(player::enqueueUnequipItem);
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Освободить слот (о — оружие, б — броня).");
    }

}
