package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;

public class SearchCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("искать", "и");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        if (!player.enqueueEvent(new SearchEvent())) {
            player.log("Не удалось провести поиски.");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Искать какие-либо полезные предметы вокруг (требует 2 единицы энергии)");
    }

}
