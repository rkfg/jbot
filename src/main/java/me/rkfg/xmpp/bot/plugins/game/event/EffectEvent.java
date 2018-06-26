package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.IAttachDetachEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class EffectEvent extends AbstractEvent {

    public static final String EFFECT_TYPE = "effect";
    public static final TypedAttribute<IEffect> ATTACH_EFFECT = TypedAttribute.of("attacheffect");
    public static final TypedAttribute<String> DETACH_EFFECT = TypedAttribute.of("detacheffect");

    public EffectEvent(IEffect effectToAttach) {
        super(EFFECT_TYPE, effectToAttach.getSource());
        setAttribute(ATTACH_EFFECT, effectToAttach);
    }

    public EffectEvent(String effectToDetach, IGameObject source) {
        super(EFFECT_TYPE, source);
        setAttribute(DETACH_EFFECT, effectToDetach);
    }

    @Override
    public void apply() {
        if (!(target instanceof IAttachDetachEffect)) {
            log.warn("Invalid target class for {}: {}", getType(), target.getClass().getName());
        }
        // constructors are mutually exclusive so only one line will fire
        getAttribute(ATTACH_EFFECT).ifPresent(((IAttachDetachEffect) target)::attachEffect);
        getAttribute(DETACH_EFFECT).ifPresent(((IAttachDetachEffect) target)::detachEffect);
    }

}
