package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class UnknownCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return emptySet();
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of("неизвестная команда, для просмотра всех доступных команд используйте %гм ман");
    }

    @Override
    public Optional<String> getHelp() {
        return exec(null, null);
    }

}
