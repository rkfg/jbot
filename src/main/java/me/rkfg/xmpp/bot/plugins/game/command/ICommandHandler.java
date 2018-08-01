package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public interface ICommandHandler {

    String getCommand();

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

    default IItem getBackpackItem(Stream<String> args, IPlayer player) {
        List<IItem> backpack = player.getBackpack();
        return getFirstIntegerArg(args).map(i -> i - 1).filter(idx -> idx >= 0 && idx < backpack.size()).map(backpack::get)
                .orElseThrow(NumberFormatException::new);
    }

    default IItem getBackpackItem(Integer itemIdx, IPlayer player) {
        List<IItem> backpack = player.getBackpack();
        return Optional.of(itemIdx).map(i -> i - 1).filter(idx -> idx >= 0 && idx < backpack.size()).map(backpack::get)
                .orElseThrow(NumberFormatException::new);
    }

    default IPlayer getPlayer(Stream<String> args) {
        List<IPlayer> playersList = World.THIS.listPlayers();
        return getFirstIntegerArg(args).filter(v -> v > 0 && v <= playersList.size()).map(i -> playersList.get(i - 1))
                .orElseThrow(NumberFormatException::new);
    }

    String getFormattedCommand();

    void setFormattedCommand(String formattedCommand);
}
