package me.rkfg.xmpp.bot.plugins.game.misc;

public interface IMutableStats extends IHasStats {

    default void changeStat(TypedAttribute<Integer> attr, Integer diff, boolean nonNegative) {
        TypedAttributeMap stats = getAttrs();
        stats.get(attr).ifPresent(s -> stats.put(attr, nonNegative ? Math.max(s + diff, 0) : s + diff)); // clamped to zero
    }
}
