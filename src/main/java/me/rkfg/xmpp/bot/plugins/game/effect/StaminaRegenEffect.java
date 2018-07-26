package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.BattleInviteEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class StaminaRegenEffect extends AbstractEffect implements IBattleEffect {

    private static final int IDLE_WARN = 2;
    public static final String TYPE = "stmregen";
    public static final TypedAttribute<Integer> REGEN = TypedAttribute.of("regen");
    public static final TypedAttribute<Integer> IDLE = TypedAttribute.of("idle");
    public static final Integer IDLE_LIMIT = 10;

    public StaminaRegenEffect(int regenPerTick) {
        super(TYPE, "регенерация стамины");
        setAttribute(REGEN, regenPerTick);
        setAttribute(IDLE, 0);
    }

    public StaminaRegenEffect() {
        this(1);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(BattleInviteEvent.TYPE) && imAttacker(event)) {
            setAttribute(IDLE, 0);
            target.enqueueDetachEffect(CowardEffect.TYPE);
        }
        if (!event.isOfType(TickEvent.TYPE)) {
            return super.processEvent(event);
        }
        return target.as(PLAYER_OBJ).map(p -> {
            if (!p.isAlive()) {
                return null;
            }
            processCowardness();
            int tired = p.getStat(HP) / 2 + drn();
            int wired = p.getStat(STM) + drn();
            if (tired > wired) {
                LoggerFactory.getLogger(getClass()).debug("{} stamina+++", p.getId());
                int regen = getAttribute(REGEN).orElse(0);
                p.log("+%d энергии, итого %d", regen, p.getStat(STM) + regen);
                p.flushLogs();
                return singleEvent(new StatsEvent().setAttributeChain(STM, regen));
            } else {
                LoggerFactory.getLogger(getClass()).debug("{} 0 stamina", p.getId());
                return null;
            }
        }).orElseGet(this::noEvent);
    }

    private void processCowardness() {
        if (target.hasEffect(CowardEffect.TYPE)) {
            return;
        }
        changeAttribute(IDLE, 1);
        Integer idle = getAttribute(IDLE).orElse(0);
        if (idle == IDLE_LIMIT - IDLE_WARN) {
            target.log("Вы слишком долго сидите без активных действий. "
                    + "Через %d секунд вы будете ссыклом и вряд ли сможете найти какие-либо предметы.", World.TICKRATE * IDLE_WARN);
            target.flushLogs();
        }
        if (idle == IDLE_LIMIT) {
            target.enqueueAttachEffect(new CowardEffect());
            target.log("Вы слишком долго сидите без активных действий и стали ссыклом.");
            target.flushLogs();
        }
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
