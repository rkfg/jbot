package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StaminaRegenEffect extends AbstractEffect {

    public static final String TYPE = "stmregen";
    public static final TypedAttribute<Integer> REGEN = TypedAttribute.of("regen");

    public StaminaRegenEffect(int regenPerTick) {
        super(TYPE, "регенерация стамины");
        setAttribute(REGEN, regenPerTick);
    }

    public StaminaRegenEffect() {
        this(1);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (!event.isOfType(TickEvent.TYPE)) {
            return super.processEvent(event);
        }
        return target.as(PLAYER_OBJ).map(p -> {
            int tired = p.getStat(HP) / 2 + drn();
            int wired = p.getStat(STM) + drn();
            if (tired > wired) {
                LoggerFactory.getLogger(getClass()).debug("{} stamina+++", p.getId());
                return singleEvent(new StatsEvent().setAttributeChain(STM, getAttribute(REGEN).orElse(0)));
            } else {
                LoggerFactory.getLogger(getClass()).debug("{} 0 stamina", p.getId());
                return null;
            }
        }).orElseGet(this::noEvent);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
