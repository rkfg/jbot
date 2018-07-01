package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UseEvent;

public interface IUseEffect extends IEffect {

    @Override
    default Collection<IEvent> processEvent(IEvent event) {
        if (!event.isOfType(UseEvent.TYPE)) {
            return noEvent();
        }
        return applyEffect(event.getTarget());
    }

    default Optional<Integer> getIntParameter(int idx) {
        return getAttribute(EFFECT_PARAMS).map(p -> {
            if (p.size() <= idx) {
                return null;
            }
            try {
                return Integer.valueOf(p.get(idx));
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    default Integer getIntParameter(int idx, int def) {
        return getIntParameter(idx).orElse(def);
    }

    Collection<IEvent> applyEffect(IGameObject target);
}
