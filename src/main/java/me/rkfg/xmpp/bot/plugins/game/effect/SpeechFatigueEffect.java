package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SpeechEvent.Volume;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class SpeechFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "speechfatigue";
    public static final TypedAttribute<Integer> WHISPER_FATIGUE = TypedAttribute.of("whisperfatigue");
    public static final int CHARS_PER_FATIGUE = 100;

    public SpeechFatigueEffect(int yellStmCost, int whisperStmCost) {
        super(TYPE, "усталость от ора");
        initFatigue(yellStmCost);
        setAttribute(WHISPER_FATIGUE, whisperStmCost);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        String msg = "Вы слишком устали, чтобы ";
        Volume volume = event.getAttribute(SpeechEvent.VOLUME).orElse(Volume.WHISPER);
        if (volume == Volume.WHISPER) {
            msg += "шептать";
        } else {
            msg += "орать";
        }
        msg += ". Попробуйте сообщение покороче или накопите энергию.";
        int length = event.getAttribute(SpeechEvent.MESSAGE).map(String::length).orElse(0) / CHARS_PER_FATIGUE + 1;
        return processFatigue(event, msg, e -> e.isOfType(SpeechEvent.TYPE), e -> e.getAttribute(SpeechEvent.VOLUME).flatMap(v -> {
            if (v == SpeechEvent.Volume.WHISPER) {
                return getAttribute(WHISPER_FATIGUE);
            }
            return getAttribute(FATIGUE);
        }).map(f -> f * length));
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
