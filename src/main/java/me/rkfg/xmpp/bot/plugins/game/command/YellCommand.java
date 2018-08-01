package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.SpeechFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent.Volume;

public class YellCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "орнуть";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            String message = args.reduce((a, s) -> a + " " + s).orElseThrow(NumberFormatException::new);
            if (player.enqueueEvent(new SpeechEvent(player, player, message, Volume.YELL))) {
                World.THIS.broadcastEvent(player, p -> new SpeechEvent(player, p, message, Volume.YELL));
                return Optional.of(String.format("Вы орёте: «%s».", message));
            } else {
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            return getHelp();
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("проорать что-нибудь на всю игровую зону. Укажите текст сообщения. За каждые "
                + SpeechFatigueEffect.CHARS_PER_FATIGUE + " символов снимется " + Player.YELL_FATIGUE_COST + " энергии.");
    }
}
