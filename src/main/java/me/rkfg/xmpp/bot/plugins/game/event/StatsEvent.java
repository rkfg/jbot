package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEvent extends AbstractEvent {

    public static final String STAT_EVENT_TYPE = "statchange";

    public StatsEvent(IGameObject source) {
        super(STAT_EVENT_TYPE, source);
    }

    @Override
    public void apply() {
        if (!(target instanceof Player)) {
            log.warn("Invalid stat event target of type {}", target.getClass().getName());
            return;
        }
        for (TypedAttribute<Integer> attr : Player.STATS) {
            getAttribute(attr).ifPresent(s -> ((Player) target).changeStat(attr, s));
        }
    }
}
