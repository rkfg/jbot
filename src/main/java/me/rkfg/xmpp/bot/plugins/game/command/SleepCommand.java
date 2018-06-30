package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;

public class SleepCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("спать", "с");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        Optional<String> arg = args.findFirst();
        if (arg.isPresent()) {
            try {
                SleepType sleepType = arg.map(Integer::valueOf).filter(v -> v >= 0 && v < 3).map(v -> SleepType.values()[v])
                        .orElseThrow(NumberFormatException::new);
                player.enqueueEvent(new SetSleepEvent(sleepType, player));
            } catch (NumberFormatException e) {
                return getHelp();
            }
        } else {
            return player.findEffect(SleepEffect.TYPE).map(
                    e -> "Выбранный режим сна: " + e.getAttribute(SleepEffect.SLEEP_TYPE_ATTR).map(SleepType::getLocalized).orElse(""));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Указать режим сна цифрой: 0 — глубокий сон, 1 — сон вполглаза, 2 — бодрствование.");
    }
}
