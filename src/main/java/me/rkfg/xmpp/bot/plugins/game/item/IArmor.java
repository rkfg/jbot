package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IArmor extends IItem {
    @Override
    default Optional<TypedAttribute<IItem>> getFittingSlot() {
        return Optional.of(ARMOR_SLOT);
    }
}
