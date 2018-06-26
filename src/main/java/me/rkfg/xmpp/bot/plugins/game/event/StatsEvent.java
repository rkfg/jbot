package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StatsEvent extends AbstractEvent {

    public static final String TYPE = "statchange";
    public static final TypedAttribute<String> COMMENT = TypedAttribute.of("comment");

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
                    getAttribute(COMMENT).ifPresent(m -> target.log(m));
                }
            });
        }
    }
}
