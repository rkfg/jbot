package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.PLAYER_OBJ;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RechargeEvent;

public class RechargeEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "recharge";
    private static final Function<IGameObject, Boolean> F_RECHARGE = w -> w.enqueueEvent(new RechargeEvent());

    public RechargeEffect() {
        super(TYPE, "перезарядка оружия и брони");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject target) {
        return target.as(PLAYER_OBJ).flatMap(p -> getParameterByKey("type").map(type -> {
            final Predicate<IGameObject> matchChargeable = w -> w.hasMatchingEffect(ChargeableEffect.TYPE, ChargeableEffect.CHARGETYPE,
                    type);
            boolean weaponRecharged = p.getWeapon().filter(matchChargeable).map(F_RECHARGE).orElse(false);
            boolean armorRecharged = p.getArmor().filter(matchChargeable).map(F_RECHARGE).orElse(false);
            if (!weaponRecharged && !armorRecharged) {
                p.log("Сначала наденьте броню или оружие, которые можно перезарядить.");
                return cancelEvent();
            }
            return null;
        })).orElseGet(this::noEvent);
    }
}
