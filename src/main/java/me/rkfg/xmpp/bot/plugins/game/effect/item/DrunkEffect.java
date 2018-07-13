package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.ITemporaryEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class DrunkEffect extends StatsEffect implements ITemporaryEffect {

    public static final String TYPE = "drunk";
    private static final TypedAttribute<Integer> CONCENTRATION = TypedAttribute.of("concentration");

    public DrunkEffect(Integer concentration) {
        super(TYPE, "пьян");
        initTemporary(5);
        setAttribute(CONCENTRATION, concentration);
    }

    @Override
    public void onBeforeAttach() {
        target.as(PLAYER_OBJ).ifPresent(p -> getAttribute(CONCENTRATION).ifPresent(c -> {
            if (p.hasTrait(AlcoEffect.TYPE)) {
                setStatChange(STR, 2 * c);
                setStatChange(ATK, c);
                setStatChange(DEF, -c);
            } else {
                setStatChange(ATK, -c);
                setStatChange(DEF, -c);
                target.enqueueEvent(new StatsEvent().setAttributeChain(HP, (int) Math.round(c * 1.5)));
            }
        }));
        super.onBeforeAttach();
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processTemporary(event, TYPE);
    }

}
