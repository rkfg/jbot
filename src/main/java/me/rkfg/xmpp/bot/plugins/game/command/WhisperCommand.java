package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Arrays.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent.Volume;

public class WhisperCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return asList("шепнуть");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            List<String> argslist = args.collect(Collectors.toList());
            String message = argslist.stream().skip(1).reduce((a, s) -> a + " " + s).orElseThrow(NumberFormatException::new);
            IPlayer target = getPlayer(argslist.stream());
            if (target == player) {
                return Optional.of("Не надо говорить с собой.");
            }
            if (player.enqueueEvent(new SpeechEvent(player, target, message, Volume.WHISPER))) {
                return Optional.of(String.format("Вы прошептали «%s». %s напряжённо вслушивается.", message, target.getName()));
            } else {
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            return getHelp();
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of(
                "прошептать что-нибудь другому игроку. Укажите номер игрока (см. %гм игроки) и далее текст сообщения. За каждые 50 символов снимется 1 энергия.");
    }
}
