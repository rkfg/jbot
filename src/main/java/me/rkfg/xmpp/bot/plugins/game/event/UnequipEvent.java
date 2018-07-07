package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.ITEM;
import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.MUTABLEPLAYER_OBJ;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.exception.NotEquippableException;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class UnequipEvent extends AbstractEvent {

    public static final String TYPE = "unequipevent";
    public static final TypedAttribute<TypedAttribute<ISlot>> SLOT_ATTR = TypedAttribute.of("slotattr");

    public class UnequippedEvent extends AbstractEvent {

        public static final String TYPE = "unequippedvent";

        public UnequippedEvent(IItem item) {
            super(TYPE);
            setAttribute(ITEM, item);
        }

    }

    public UnequipEvent(TypedAttribute<ISlot> slot) {
        super(TYPE);
        setAttribute(SLOT_ATTR, slot);
    }

    @Override
    public void apply() {
        // this is a multipipe event, we'll execute it later
    }

    public void unequip() {
        getAttribute(SLOT_ATTR).ifPresent(s -> target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
            try {
                Optional<IItem> item = p.getSlot(s).flatMap(ISlot::getItem);
                p.unequipItem(s);
                setDescription(String.format("Вы освобождаете слот [%s]", p.getSlot(s).flatMap(ISlot::getDescription).orElse(s.getName())));
                logTargetComment();
                item.ifPresent(i -> p.enqueueEvent(new UnequippedEvent(i)));
            } catch (NotEquippableException e) {
                target.log("Не удалось надеть предмет: " + e.getMessage());
                setCancelled();
            }
        }));
    }

}
