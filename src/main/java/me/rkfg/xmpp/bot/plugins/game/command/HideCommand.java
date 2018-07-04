package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;

public class HideCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("спрятаться", "спр");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        if (player.hasEffect(HideEffect.TYPE)) {
            player.enqueueDetachEffect(HideEffect.TYPE);
        } else {
            player.enqueueAttachEffect(new HideEffect());
        }
        return Optional.empty();
    }

}
