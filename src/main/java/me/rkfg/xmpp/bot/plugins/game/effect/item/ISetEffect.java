package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.capitalize;

import java.util.Collection;
import java.util.HashMap;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent.EquippedEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent.UnequippedEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;

public interface ISetEffect extends IEffect {

    default void initSet(String otherType) {
        setAttribute(OTHERTYPE, otherType);
    }

    @Override
    default Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(EquippedEvent.TYPE)) {
            return event.getTarget().as(PLAYER_OBJ).map(p -> {
                boolean armorFitsWeapon = getTarget().as(WEAPON_OBJ).flatMap(w -> p.getArmor().map(IArmor::getType))
                        .filter(t -> t.equals(getAttribute(OTHERTYPE).orElse(""))).isPresent();
                boolean weaponFitsArmor = getTarget().as(ARMOR_OBJ).flatMap(a -> p.getWeapon().map(IWeapon::getType))
                        .filter(t -> t.equals(getAttribute(OTHERTYPE).orElse(""))).isPresent();
                if (!isSetEffectActive() && (armorFitsWeapon || weaponFitsArmor)) {
                    if (armorFitsWeapon) { // only say it once
                        getTarget().log("Сет собран.");
                    }
                    return onSetEffectActivate();
                }
                return noEvent();
            }).orElseGet(this::noEvent);
        }
        if (event.isOfType(UnequippedEvent.TYPE) && isSetEffectActive()) {
            getTarget().getDescription().ifPresent(d -> getTarget().log("%s больше не состоит в сете.", capitalize(d)));
            return onSetEffectDeactivate();
        }
        return IEffect.super.processEvent(event);
    }

    default Collection<IEvent> onSetEffectDeactivate() {
        getAttribute(EFFECT_PARAMS_KV).ifPresent(kv -> {
            final ItemStatEffect effect = new ItemStatEffect();
            HashMap<String, String> reverse = new HashMap<>(kv); // reverse the stat change
            reverse.entrySet().forEach(e -> {
                try {
                    reverse.put(e.getKey(), "" + -Integer.valueOf(e.getValue()));
                } catch (NumberFormatException ex) {
                    // skip non-integer keys
                }
            });
            effect.setAttribute(EFFECT_PARAMS_KV, reverse);
            effect.applyEffect(getTarget());
            setAttribute(SETEFFECTACTIVE, false);
        });
        return noEvent();
    }

    default Boolean isSetEffectActive() {
        return getAttribute(SETEFFECTACTIVE).orElse(false);
    }

    default Collection<IEvent> onSetEffectActivate() {
        getAttribute(EFFECT_PARAMS_KV).ifPresent(kv -> {
            final ItemStatEffect effect = new ItemStatEffect();
            effect.setAttribute(EFFECT_PARAMS_KV, kv);
            effect.applyEffect(getTarget());
            setAttribute(SETEFFECTACTIVE, true);
        });
        return noEvent();
    }
}
