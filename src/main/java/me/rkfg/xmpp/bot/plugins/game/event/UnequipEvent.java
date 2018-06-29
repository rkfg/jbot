package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.exception.NotEquippableException;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class UnequipEvent extends AbstractEvent {

    public static final String TYPE = "unequipevent";
    public static final TypedAttribute<TypedAttribute<ISlot>> SLOT_ATTR = TypedAttribute.of("slotattr");

    public UnequipEvent(IGameObject source, TypedAttribute<ISlot> slot) {
        super(TYPE, source);
        setAttribute(SLOT_ATTR, slot);
    }

    @Override
    public void apply() {
        getAttribute(SLOT_ATTR).ifPresent(s -> target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
            try {
                p.unequipItem(s);
                setDescription(String.format("Вы освобождаете слот [%s]", p.getSlot(s).flatMap(ISlot::getDescription).orElse(s.getName())));
                super.apply();
            } catch (NotEquippableException e) {
                target.log("Не удалось надеть предмет: " + e.getMessage());
            }
        }));
    }

}
