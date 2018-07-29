package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

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
        getAttribute(SLOT_ATTR)
                .ifPresent(s -> target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.getSlot(s).flatMap(ISlot::getItem).ifPresent(item -> {
                    try {
                        p.unequipItem(s);
                        String unequipMessage = "Вы освобождаете слот [%s] и убираете %s в рюкзак.";
                        if (!p.isAlive()) {
                            unequipMessage = "Противник освобождает ваш слот [%s] и убирает %s в рюкзак.";
                        }
                        setDescription(String.format(unequipMessage, s.getName(), item.getItemDescription()));
                        logTargetComment();
                        p.enqueueEvent(new UnequippedEvent(item));
                    } catch (NotEquippableException e) {
                        target.log("Не удалось снять %s: %s.", item.getItemDescription(), e.getMessage());
                        cancel();
                    }
                })));
    }

}
