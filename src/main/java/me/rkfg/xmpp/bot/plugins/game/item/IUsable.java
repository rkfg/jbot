package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IUsable extends IItem {

    @Override
    default Optional<TypedAttribute<IItem>> getFittingSlot() {
        return Optional.empty();
    }
}
