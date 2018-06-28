package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StaminaRegenEffect extends AbstractEffect {

    public static final String TYPE = "stmregen";
    public static final TypedAttribute<Integer> REGEN = TypedAttribute.of("regen");

    public StaminaRegenEffect(IGameObject source, int regenPerTick) {
        super(TYPE, "регенерация стамины", source);
        setAttribute(REGEN, regenPerTick);
    }

    public StaminaRegenEffect(IGameObject source) {
        this(source, 1);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (!event.isOfType(TickEvent.TYPE)) {
            return super.processEvent(event);
        }
        return singleEvent(new StatsEvent(source).setAttributeChain(STM, getAttribute(REGEN).orElse(0)));
    }

}
