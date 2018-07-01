package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;

public class CursedEffect extends AbstractEffect {

    public static final String TYPE = "cursed";

    public CursedEffect() {
        super(TYPE, "нельзя снять предмет");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(UnequipEvent.TYPE)) {
            event.getTarget().log("Нельзя снимать предмет %s", unboxString(target.getDescription()));
            return cancelEvent();
        }
        return noEvent();
    }

}
