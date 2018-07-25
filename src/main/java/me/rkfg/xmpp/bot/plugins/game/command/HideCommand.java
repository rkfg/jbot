package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;

public class HideCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("спрятаться");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        player.enqueueToggleEffect(new HideEffect());
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Попытаться спрятаться в складках местности и кустах, расходует " + Player.HIDE_FATIGUE_COST
                + " единиц энергии. При определённой доле везения, атакующий не сможет вас найти, и бой не состоится.");
    }
}
