package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Arrays.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;

public class ReadyCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return asList("готов");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return World.THIS.setPlayerReady(player, true);
    }

}
