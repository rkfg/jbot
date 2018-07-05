package me.rkfg.xmpp.bot.plugins.game.misc;

public interface IMutableStats extends IHasStats {
    
    default void changeStat(TypedAttribute<Integer> attr, Integer diff) {
        TypedAttributeMap stats = getAttrs();
        stats.get(attr).ifPresent(s -> stats.put(attr, Math.max(s + diff, 0))); // clamped to zero
    }
}
