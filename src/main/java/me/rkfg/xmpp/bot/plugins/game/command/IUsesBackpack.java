package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.List;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public interface IUsesBackpack extends ICommandHandler {
    default IItem getBackpackItem(Stream<String> args, IPlayer player) {
        List<IItem> backpack = player.getBackpack();
        return getFirstIntegerArg(args).map(i -> i - 1).filter(idx -> idx >= 0 && idx < backpack.size()).map(idx -> backpack.get(idx))
                .orElseThrow(NumberFormatException::new);
    }
}
