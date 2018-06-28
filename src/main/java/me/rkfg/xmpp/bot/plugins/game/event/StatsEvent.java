package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEvent extends AbstractEvent {

    public static final String TYPE = "statchange";

    public StatsEvent(IGameObject source) {
        super(TYPE, source);
    }

    @Override
    public void apply() {
        for (TypedAttribute<Integer> attr : STATS) {
            getAttribute(attr).ifPresent(s -> target.as(MUTABLEPLAYER_OBJ).ifPresent(player -> {
                int oldStat = player.getStat(attr);
                player.changeStat(attr, s);
                if (player.getStat(attr) != oldStat) {
                    super.apply();
                }
                if (player.getStat(HP) < 1) {
                    player.setDead(true);
                }
                if (player.getStat(STM) > 15) {
                    player.changeStat(STM, Math.min(15 - player.getStat(STM), 0));
                }
            }));
        }
    }

    public <T> StatsEvent setAttributeChain(TypedAttribute<T> attr, T value) {
        super.setAttribute(attr, value);
        return this;
    }
}
