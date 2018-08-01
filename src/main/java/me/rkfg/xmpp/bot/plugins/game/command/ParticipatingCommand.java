package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;

public class ParticipatingCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "участвую";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        Optional<Integer> arg = getFirstIntegerArg(args);
        if (arg.isPresent() && arg.get().equals(0)) {
            return World.THIS.setPlayerState(player, GamePlayerState.NONE);
        }
        return World.THIS.setPlayerState(player, GamePlayerState.GATHER);
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Зарегистрироваться на игру. Отменить участие можно с помощью " + GamePlugin.CMD + "участвую 0");
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

}
