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
        setDescription(String.format("Вы пытаетесь надеть предмет [%s]", item.getDescription().orElse("<предмет>")));
    }

    @Override
    public void apply() {
        super.apply();
        getAttribute(ITEM).ifPresent(i -> target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
            try {
                p.equipItem(i);
            } catch (NotEquippableException e) {
                target.log("Не удалось надеть предмет: " + e.getMessage());
            }
        }));
    }

}
