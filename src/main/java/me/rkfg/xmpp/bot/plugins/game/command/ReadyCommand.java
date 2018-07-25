package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Arrays.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;

public class ReadyCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return asList("готов");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        Optional<Integer> arg = getFirstIntegerArg(args);
        if (arg.isPresent() && arg.get().equals(0)) {
            return World.THIS.setPlayerState(player, GamePlayerState.GATHER);
        } else {
            return World.THIS.setPlayerState(player, GamePlayerState.READY);
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Проголосовать за начало игры. Игра начнётся, когда готовы будут 75% игроков. "
                + "Отменить готовность можно с помощью " + GamePlugin.CMD + "готов 0");
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

}
