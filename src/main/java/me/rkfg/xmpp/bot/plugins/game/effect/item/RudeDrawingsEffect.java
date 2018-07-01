package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.ITemporaryEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class RudeDrawingsEffect extends StatsEffect implements ITemporaryEffect {

    public static final String TYPE = "rudedrawingseffect";

    public RudeDrawingsEffect() {
        super(TYPE, "изрисован непристойностями");
        setStatChange(DEF, -2);
        initTemporary(5);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processTemporary(event, TYPE);
    }

}
