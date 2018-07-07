package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;

public class AmbushCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("засада");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        player.enqueueToggleEffect(new AmbushEffect());
        return Optional.empty();
    }

}
