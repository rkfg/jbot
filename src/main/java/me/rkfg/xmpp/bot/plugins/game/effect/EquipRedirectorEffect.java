package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;

public class EquipRedirectorEffect extends AbstractEffect {

    public static final String TYPE = "equipredirector";

    public EquipRedirectorEffect() {
        super(TYPE, "перенаправление эквипа/анэквипа на предмет");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(EquipEvent.TYPE) && !event.getAttribute(EquipEvent.ITEM).map(i -> i.enqueueEvent(event)).orElse(true)
                || event.isOfType(UnequipEvent.TYPE) && !event.getAttribute(UnequipEvent.SLOT_ATTR)
                        .flatMap(s -> target.as(PLAYER_OBJ).flatMap(p -> p.getSlot(s).flatMap(ISlot::getItem)))
                        .map(i -> i.enqueueEvent(event)).orElse(true)) {
            return cancelEvent();
        }
        return super.processEvent(event);

    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
