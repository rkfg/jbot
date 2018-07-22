package me.rkfg.xmpp.bot.plugins.game.effect.trait;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IBattleEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class BazookaHandsEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "bazookahandseffect";

    private class BazookaHandsStats extends StatsEffect {
        public static final String TYPE = "bazookahandsstats";

        public BazookaHandsStats() {
            super(TYPE, "эффект рук-базук");
            setStatChange(ATK, 2);
            setStatChange(STR, 2);
        }
    }

    public BazookaHandsEffect() {
        super(TYPE, "руки-базуки");
    }

    @Override
    public Collection<IEvent> battleBegins(IEvent event) {
        target.as(PLAYER_OBJ).filter(p -> !p.getWeapon().isPresent()).ifPresent(p -> p.enqueueAttachEffect(new BazookaHandsStats()));
        return noEvent();
    }

    @Override
    public Collection<IEvent> battleEnds(IEvent event) {
        target.enqueueDetachEffect(BazookaHandsStats.TYPE);
        return noEvent();
    }
    
    @Override
    public boolean isVisible() {
        return false;
    }

}
