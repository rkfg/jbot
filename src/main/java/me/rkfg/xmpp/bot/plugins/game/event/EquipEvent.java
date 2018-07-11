package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.exception.NotEquippableException;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class EquipEvent extends AbstractEvent {

    public static final String TYPE = "equipevent";

    public class EquippedEvent extends AbstractEvent {

        public static final String TYPE = "equippedvent";

        public EquippedEvent() {
            super(TYPE);
        }

    }

    public EquipEvent(IItem item) {
        super(TYPE);
        setAttribute(ITEM, item);
    }

    @Override
    public void apply() {
        // this is a multipipe event, we'll execute it later
    }

    public void equip() {
        getAttribute(ITEM).ifPresent(i -> target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
            String desc = i.getDescription().orElse("<предмет>");
            try {
                p.equipItem(i);
                setDescription(String.format("Вы надеваете %s", desc));
                logTargetComment();
                p.enqueueEvent(new EquippedEvent());
            } catch (NotEquippableException e) {
                target.log("Не удалось надеть %s: %s", desc, e.getMessage());
                setCancelled();
            }
        }));
    }

}
