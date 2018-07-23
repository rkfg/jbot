package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class ExpiringBonusPointsEffect extends AbstractEffect implements ITemporaryEffect {

    public static final String TYPE = "expiringpoints";
    public static final TypedAttribute<Integer> EXPIRING_POINTS = TypedAttribute.of("expiringPoints");
    public static final TypedAttribute<Integer> TICKS_BEFORE_EXPIRING = TypedAttribute.of("ticksBeforeExpiring");

    public ExpiringBonusPointsEffect(int points, int ticks) {
        super(TYPE, "удаление поинтов по таймауту");
        initTemporary(ticks);
        setAttribute(EXPIRING_POINTS, points);
    }

    @Override
    public void onAfterAttach() {
        target.log("У вас есть сгорающие очки стат (%d шт.), которые обнулятся через %d секунд. Успейте их потратить!",
                getAttribute(EXPIRING_POINTS).orElse(0), getAttribute(LIFETIME).orElse(0) * World.TICKRATE);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processTemporary(event, TYPE);
    }

    @Override
    public void onAfterDetach() {
        target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> getAttribute(EXPIRING_POINTS).ifPresent(pts -> {
            p.changeAttribute(BONUS_POINTS, -pts);
            if (pts > 0) {
                p.log("Бонусные очки стат (%d шт.) сгорели.", pts);
                p.flushLogs();
            }
        }));
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
