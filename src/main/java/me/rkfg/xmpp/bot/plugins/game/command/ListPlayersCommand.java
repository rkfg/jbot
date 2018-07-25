package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;

public class ListPlayersCommand implements ICommandHandler {

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        List<IPlayer> playersList = World.THIS.listPlayers().stream().filter(p -> p.getState() != GamePlayerState.NONE)
                .collect(Collectors.toList());
        if (playersList.isEmpty()) {
            return Optional.of("Игроков нет.");
        }
        return IntStream.range(0, playersList.size()).mapToObj(i -> {
            GamePlayerState worldState = World.THIS.getState();
            final IPlayer p = playersList.get(i);
            StringBuilder desc = new StringBuilder();
            desc.append(i + 1).append(": ");
            if (worldState != GamePlayerState.PLAYING) {
                desc.append(p.getId());
            } else {
                desc.append(p.getName());
            }
            if (!p.isAlive()) {
                desc.append(" [мёртв]");
            }
            if (p == player) {
                desc.append(" [вы]");
            }
            if (worldState != GamePlayerState.PLAYING) {
                if (p.getState() == GamePlayerState.GATHER) {
                    desc.append(" [участвует]");
                }
                if (p.getState() == GamePlayerState.READY) {
                    desc.append(" [готов]");
                }
            }
            return desc.toString();
        }).reduce(commaReducer).map(list -> "Игроки: " + list);
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Вывести список всех участников.");
    }

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("игроки");
    }

    @Override
    public boolean deadAllowed() {
        return true;
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

}
