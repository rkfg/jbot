package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RechargeEvent;

public class RechargeEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "recharge";

    public RechargeEffect() {
        super(TYPE, "перезарядка оружия и брони");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject target) {
        return target.as(PLAYER_OBJ).map(p -> {
            boolean weaponRecharged = p.getWeapon().filter(w -> w.hasEffect(ChargeableEffect.TYPE))
                    .map(w -> w.enqueueEvent(new RechargeEvent())).orElse(false);
            boolean armorRecharged = p.getArmor().filter(w -> w.hasEffect(ChargeableEffect.TYPE))
                    .map(w -> w.enqueueEvent(new RechargeEvent())).orElse(false);
            if (!weaponRecharged && !armorRecharged) {
                p.log("Сначала наденьте броню или оружие, которые можно перезарядить.");
                return cancelEvent();
            }
            return null;
        }).orElseGet(this::noEvent);
    }
}