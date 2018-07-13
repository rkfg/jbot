package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public interface ICommandHandler {

    Collection<String> getCommand();

    Optional<String> exec(IPlayer player, Stream<String> args);

    default Optional<String> getHelp() {
        return Optional.of("Нет справки по этой команде.");
    }

    default boolean deadAllowed() {
        return false;
    }

    default boolean pregameAllowed() {
        return false;
    }

    default Optional<Integer> getFirstIntegerArg(Stream<String> args) {
        return args.findFirst().map(Integer::valueOf);
    }
}
