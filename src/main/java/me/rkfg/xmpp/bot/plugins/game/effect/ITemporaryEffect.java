package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;

public interface ITemporaryEffect extends IEffect {

    default void initTemporary(int ticks) {
        setAttribute(LIFETIME, ticks);
    }

    default Collection<IEvent> processTemporary(IEvent event, String type) {
        return processTemporary(event, type, null);
    }

    default Collection<IEvent> processTemporary(IEvent event, String type, String detachMessage) {
        if (event.isOfType(TickEvent.TYPE)) {
            return getAttribute(LIFETIME).map(l -> {
                if (--l < 1) {
                    if (detachMessage != null && !detachMessage.isEmpty()) {
                        getTarget().log(detachMessage);
                        getTarget().flushLogs();
                    }
                    return detachEffect(type);
                } else {
                    setAttribute(LIFETIME, l);
                }
                return null;
            }).orElseGet(this::noEvent);
        }
        return noEvent();
    }
}
