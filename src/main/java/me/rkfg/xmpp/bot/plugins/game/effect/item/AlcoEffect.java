package me.rkfg.xmpp.bot.plugins.game.effect.item;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasTraits;

public class AlcoEffect extends AbstractEffect implements IUseEffect, IHasTraits {

    public static final String TYPE = "alco";

    public AlcoEffect() {
        super(TYPE, "содержит алкоголь");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject target) {
        getIntParameter(0).ifPresent(conc -> target.enqueueAttachEffect(new DrunkEffect(conc)));
        return noEvent();
    }

}
