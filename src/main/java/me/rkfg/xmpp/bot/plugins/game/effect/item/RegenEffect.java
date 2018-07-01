package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;

public class RegenEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "regen";

    public RegenEffect() {
        super(TYPE, "регенерация");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject target) {
        final Integer hp = getIntParameter(0, 2);
        target.log("Вы вылечиваетесь на %d здоровья.", hp);
        target.enqueueEvent(new StatsEvent().setAttributeChain(HP, hp));
        return noEvent();
    }

}
