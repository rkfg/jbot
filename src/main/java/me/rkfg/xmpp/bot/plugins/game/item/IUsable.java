package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IUsable extends IItem {

    @Override
    default Optional<TypedAttribute<ISlot>> getFittingSlot() {
        return Optional.of(ITEM_SLOT);
    }

    @Override
    default void onUse() {
        getAttribute(USE_CNT).ifPresent(c -> {
            if (--c < 1) {
                getOwner().flatMap(o -> o.as(MUTABLEPLAYER_OBJ)).ifPresent(p -> p.removeFromBackpack(this));
            } else {
                setAttribute(USE_CNT, c);
            }
        });
    }
}
