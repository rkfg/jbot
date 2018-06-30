package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public interface ICommandHandler {

    Collection<String> getCommand();

    Optional<String> exec(IPlayer player, Stream<String> args);

    default Optional<String> getHelp() {
        return Optional.empty();
    }
    
    default boolean deadAllowed() {
        return false;
    }
}
