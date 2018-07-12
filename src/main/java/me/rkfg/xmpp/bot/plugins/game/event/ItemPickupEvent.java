package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class ItemPickupEvent extends AbstractEvent {

    public static final String TYPE = "itempickup";
    public static final TypedAttribute<IItem> ITEM = TypedAttribute.of("item");

    public ItemPickupEvent(IItem item) {
        super(TYPE);
        setAttribute(ITEM, item);
        item.getDescription().ifPresent(d -> setDescription(String.format("Вы кладёте %s в рюкзак.", capitalize(d))));
    }

    @Override
    public void apply() {
        super.apply();
        target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> getAttribute(ITEM).ifPresent(p::putItemToBackpack));
    }

}
