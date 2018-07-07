package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class AmbiguousCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return emptySet();
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of("неоднозначная команда, используйте более полную форму.");
    }

}
