package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

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
        return singleEvent(new StatsEvent(source).setAttributeChain(STM, getAttribute(REGEN).orElse(0)));
    }

}
