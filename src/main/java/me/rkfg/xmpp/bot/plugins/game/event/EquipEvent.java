package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.exception.NotEquippableException;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class EquipEvent extends AbstractEvent {

    public static final String TYPE = "equipevent";
    public static final TypedAttribute<IItem> ITEM = TypedAttribute.of("item");

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
            try {
                p.equipItem(i);
                setDescription(String.format("Вы надеваете предмет [%s]", i.getDescription().orElse("<предмет>")));
                logTargetComment();
            } catch (NotEquippableException e) {
                target.log("Не удалось надеть предмет: " + e.getMessage());
                setCancelled();
            }
        }));
    }

}
