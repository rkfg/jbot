package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDirection;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IEvent extends IHasAttributes, IHasType, IHasDirection, IHasDescription {

    public void apply();

    default <T> boolean matchByTypeAttr(String type, TypedAttribute<T> attr, T value) {
        return isOfType(type) && matchAttributeValue(attr, value);
    }

    default boolean isAttachEffect(String effectType) {
        return isOfType(EffectEvent.TYPE)
                && getAttribute(EffectEvent.ATTACH_EFFECT).filter(eff -> eff.getType().equals(effectType)).isPresent();
    }

    default boolean isDetachEffect(String effectType) {
        return matchByTypeAttr(EffectEvent.TYPE, EffectEvent.DETACH_EFFECT, effectType);
    }

    public void setCancelled();

    public boolean isCancelled();

}
