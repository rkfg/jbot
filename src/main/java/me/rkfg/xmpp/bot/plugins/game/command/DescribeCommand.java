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
        try {
            IItem item = getBackpackItem(args, player);
            player.log("Вы тщательно осматриваете %s. %s", unboxString(item.getDescription(Verbosity.WITH_PARAMS)),
                    capitalize(unboxString(item.getDescription(Verbosity.VERBOSE))));
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

}
