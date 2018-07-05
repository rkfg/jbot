package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasStats;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IWeapon extends IItem, IHasStats {

    @Override
    default Optional<TypedAttribute<ISlot>> getFittingSlot() {
        return Optional.of(WEAPON_SLOT);
    }

    default Integer getAttack() {
        return getAttribute(ATK).orElse(0);
    }

    default Integer getDefence() {
        return getAttribute(DEF).orElse(0);
    }

    default Integer getStrength() {
        return getAttribute(STR).orElse(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == WEAPON_OBJ || type == STATS_OBJ || type == MUTABLESTATS_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }
}
