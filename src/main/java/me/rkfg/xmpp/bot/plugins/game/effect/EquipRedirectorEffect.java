package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent.EquippedEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent.UnequippedEvent;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;

public class EquipRedirectorEffect extends AbstractEffect {

    public static final String TYPE = "equipredirector";

    public EquipRedirectorEffect() {
        super(TYPE, "перенаправление эквипа/анэквипа на предмет");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(EquipEvent.TYPE) && !event.getAttribute(ITEM).map(i -> i.enqueueEvent(event)).orElse(true)) {
            return cancelEvent();
        }
        if (event.isOfType(UnequipEvent.TYPE) && !event.getAttribute(UnequipEvent.SLOT_ATTR)
                .flatMap(s -> target.as(PLAYER_OBJ).flatMap(p -> p.getSlot(s).flatMap(ISlot::getItem))).map(i -> i.enqueueEvent(event))
                .orElse(true)) {
            return cancelEvent();
        }
        if (event.isOfType(EquippedEvent.TYPE)) {
            sendToEquipped(event);
        }
        if (event.isOfType(UnequippedEvent.TYPE)) {
            sendToEquipped(event);
            event.getAttribute(ITEM).ifPresent(i -> i.enqueueEvent(event));
        }
        return super.processEvent(event);

    }

    public void sendToEquipped(IEvent event) {
        event.getTarget().as(PLAYER_OBJ).ifPresent(p -> {
            p.getSlot(WEAPON_SLOT).flatMap(ISlot::getItem).ifPresent(i -> i.enqueueEvent(event));
            p.getSlot(ARMOR_SLOT).flatMap(ISlot::getItem).ifPresent(i -> i.enqueueEvent(event));
        });
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
