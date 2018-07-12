package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription.Verbosity;

public class DescribeCommand implements ICommandHandler, IUsesBackpack {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("осмотреть");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return args.findFirst().map(String::toLowerCase).flatMap(a -> {
            try {
                IItem item = getBackpackItem(Integer.valueOf(a), player);
                return describeItem(player, Optional.of(item), "Предмет не найден в рюкзаке");
            } catch (NumberFormatException e) {
                if ("о".equals(a)) {
                    return describeItem(player, player.getWeapon(), "У вас в руках ничего нет.");
                } else if ("б".equals(a)) {
                    return describeItem(player, player.getArmor(), "На вас не надето никакой брони.");
                }
                return getHelp();
            }
        });
    }

    public Optional<String> describeItem(IPlayer player, Optional<? extends IItem> item, String emptyMessage) {
        if (!item.isPresent()) {
            player.log(emptyMessage);
        } else {
            item.ifPresent(i -> player.log("Вы тщательно осматриваете %s. %s", unboxString(i.getDescription(Verbosity.WITH_PARAMS)),
                    capitalize(unboxString(i.getDescription(Verbosity.VERBOSE)))));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Получить описание предмета в рюкзаке (укажите номер), оружия в руках (буква о) или надетой брони (буква б).");
    }

}
