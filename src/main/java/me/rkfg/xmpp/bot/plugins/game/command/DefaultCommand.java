package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;

public class DefaultCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        World.THIS.defaultCommand(player);
        return Optional.empty();
    }

    @Override
    public boolean deadAllowed() {
        return true;
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

}
