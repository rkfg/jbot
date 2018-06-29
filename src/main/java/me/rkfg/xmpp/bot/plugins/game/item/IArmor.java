package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IArmor extends IItem {
    @Override
    default Optional<TypedAttribute<ISlot>> getFittingSlot() {
        return Optional.of(ARMOR_SLOT);
    }

    default Integer getDefence() {
        return getAttribute(DEF).orElse(0);
    }

    default Integer getProtection() {
        return getAttribute(PRT).orElse(0);
    }
}
