package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SpeechEvent extends AbstractEvent {

    public static final String TYPE = "speech";

    public static final TypedAttribute<String> MESSAGE = TypedAttribute.of("message");
    public static final TypedAttribute<Volume> VOLUME = TypedAttribute.of("volume");

    public enum Volume {
        WHISPER, YELL
    }

    public SpeechEvent(IPlayer source, IPlayer target, String message, Volume volume) {
        super(TYPE);
        setSource(source);
        setTarget(target);
        setAttribute(MESSAGE, message);
        setAttribute(VOLUME, volume);
    }

    @Override
    public void apply() {
        if (target == source) {
            return;
        }
        String author = target.as(PLAYER_OBJ).flatMap(p -> {
            if (p.hasTrait("deanon")) {
                return source.as(PLAYER_OBJ).map(IPlayer::getName);
            }
            return Optional.empty();
        }).orElse("Кто-то");
        String volume = getAttribute(VOLUME).flatMap(v -> {
            if (v == Volume.YELL) {
                return Optional.of("орёт на весь лес");
            }
            return Optional.empty();
        }).orElse("шепчет вам");
        target.log("%s %s: %s", author, volume, getAttribute(MESSAGE).orElse("<нерзаборчивое бормотание>"));
        target.as(PLAYER_OBJ).ifPresent(IPlayer::flushLogs);
    }

}
