package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription.Verbosity;

public class ListBackpackCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("рюкзак");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        List<IItem> backpack = player.getBackpack();
        return Optional.of("В рюкзаке можно найти: " + IntStream.range(0, backpack.size()).mapToObj(
                idx -> String.format("<u>{%d}</u>: <b>%s</b>", idx + 1, backpack.get(idx).getDescription(Verbosity.WITH_PARAMS).orElse("неизвестно")))
                .reduce(commaReducer).orElse("рюкзак пуст"));
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Получить пронумерованный список вещей в рюкзаке.");
    }

}
