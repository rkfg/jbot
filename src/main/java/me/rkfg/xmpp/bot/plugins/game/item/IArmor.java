package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IArmor extends IStatsItem {
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

    @SuppressWarnings("unchecked")
    @Override
    default <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == ARMOR_OBJ || type == STATS_OBJ || type == MUTABLESTATS_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    @Override
    default String getStatsStr() {
        return String.format(" З:%d/Б:%d [б]", getDefence(), getProtection());
    }
}
