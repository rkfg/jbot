package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;

public class SearchCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "искать";
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
        return Optional.of("Искать какие-либо полезные предметы вокруг (требует " + Player.SEARCH_FATIGUE_COST + " единицы энергии).");
    }

}
