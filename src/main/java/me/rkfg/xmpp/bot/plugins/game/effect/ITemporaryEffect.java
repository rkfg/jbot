package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;

public interface ITemporaryEffect extends IHasAttributes {

    default void initTemporary(int ticks) {
        setAttribute(LIFETIME, ticks);
    }
    
    default boolean processTemporary(IEvent event) {
        if (event.isOfType(TickEvent.TYPE)) {
            return getAttribute(LIFETIME).map(l -> {
                if (--l < 1) {
                    return false;
                } else {
                    setAttribute(LIFETIME, l);
                }
                return true;
            }).orElse(true);
        }
        return true;
    }
}
