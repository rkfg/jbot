package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class AmbiguousCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of("Неоднозначная команда, используйте более полную форму.");
    }

    @Override
    public Optional<String> getHelp() {
        return exec(null, null);
    }
    
}
