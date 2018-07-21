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

    public SpeechFatigueEffect() {
        super(TYPE, "усталость от ора");
        initFatigue(2);
        setAttribute(WHISPER_FATIGUE, 1);
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
        int length = event.getAttribute(SpeechEvent.MESSAGE).map(String::length).orElse(0) / 50 + 1;
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
