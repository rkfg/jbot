package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEvent extends AbstractEvent {

    public static final String TYPE = "statchange";

    public StatsEvent() {
        super(TYPE);
    }

    @Override
    public void apply() {
        for (TypedAttribute<Integer> attr : STATS) {
            getAttribute(attr).ifPresent(s -> target.as(MUTABLESTATS_OBJ).ifPresent(item -> {
                int oldStat = item.getStat(attr);
                item.changeStat(attr, s, item.as(PLAYER_OBJ).isPresent()); // player can't have negative stats, items can
                if (item.getStat(attr) != oldStat) {
                    super.apply();
                }
                if (item.getStat(HP) < 1) {
                    target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.setDead(true));
                }
            }));
        }
    }

    public <T> StatsEvent setAttributeChain(TypedAttribute<T> attr, T value) {
        super.setAttribute(attr, value);
        return this;
    }
}
