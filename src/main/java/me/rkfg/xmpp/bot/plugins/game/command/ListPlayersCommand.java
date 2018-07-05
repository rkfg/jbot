package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;

public class ListPlayersCommand implements ICommandHandler {

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        List<IPlayer> playersList = World.THIS.listPlayers();
        if (playersList.isEmpty()) {
            return Optional.of("Игроков нет.");
        }
        return IntStream.range(0, playersList.size()).mapToObj(i -> {
            final IPlayer p = playersList.get(i);
            return "" + (i + 1) + ": " + p.getName() + (p.isAlive() ? "" : " [мёртв]") + (p == player ? " [вы]" : "");
        }).reduce(commaReducer).map(list -> "Игроки: " + list);
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Вывести список всех участников");
    }

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("игроки", "иг");
    }

}
