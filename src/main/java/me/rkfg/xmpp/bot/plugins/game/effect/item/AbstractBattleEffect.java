package me.rkfg.xmpp.bot.plugins.game.effect.item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.AttackEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public abstract class AbstractBattleEffect extends AbstractEffect {

    public AbstractBattleEffect(String type, String description) {
        super(type, description);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(AttackEvent.TYPE)) {
            Set<IEvent> result = new HashSet<>();
            if (event.getAttribute(AttackEvent.SUCCESSFUL).orElse(false)) {
                result.addAll(attackSuccess(event));
                result.addAll(defenceFailure(event));
            } else {
                result.addAll(attackFailure(event));
                result.addAll(defenceSuccess(event));
            }
        }
        return super.processEvent(event);
    }

    protected Collection<IEvent> defenceSuccess(IEvent event) {
        return Collections.emptySet();
    }

    protected Collection<IEvent> attackFailure(IEvent event) {
        return Collections.emptySet();
    }

    protected Collection<IEvent> defenceFailure(IEvent event) {
        return Collections.emptySet();
    }

    protected Collection<IEvent> attackSuccess(IEvent event) {
        return Collections.emptySet();
    }

}
