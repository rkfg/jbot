package me.rkfg.xmpp.bot.plugins.game.effect.item;

import java.util.Collection;

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

    Collection<IEvent> applyEffect(IGameObject target);
}
