package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEvent extends AbstractEvent {

    public static final String TYPE = "statchange";

    public StatsEvent(IGameObject source) {
        super(TYPE, source);
    }

    @Override
    public void apply() {
        if (!(target instanceof IMutablePlayer)) {
            log.warn("Invalid stat event target of type {}", target.getClass().getName());
            return;
        }
        for (TypedAttribute<Integer> attr : Player.STATS) {
            getAttribute(attr).ifPresent(s -> {
                IMutablePlayer player = (IMutablePlayer) target;
                int oldStat = player.getStat(attr);
                player.changeStat(attr, s);
                if (player.getStat(attr) != oldStat) {
                    super.apply();
                }
                if (player.getStat(IPlayer.HP) < 1) {
                    player.setDead(true);
                }
                if (player.getStat(IPlayer.STM) > 15) {
                    player.changeStat(IPlayer.STM, Math.min(15 - player.getStat(IPlayer.STM), 0));
                }
            });
        }
    }

    public <T> StatsEvent setAttributeChain(TypedAttribute<T> attr, T value) {
        super.setAttribute(attr, value);
        return this;
    }
}
