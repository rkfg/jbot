package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class CowardEffect extends AbstractEffect {

    public static final String TYPE = "coward";
    public static final TypedAttribute<Integer> COWARD_PTS = TypedAttribute.of("points");
    public static final Integer MAX_COWARD = 30;

    public CowardEffect() {
        super(TYPE, "ссыкло");
    }

    @Override
    public void onBeforeAttach() {
        target.as(PLAYER_OBJ).ifPresent(p -> {
            Integer hp = p.getStat(HP);
            int cowardPts = 0;
            if (hp > 10) {
                cowardPts = hp / 3;
                log.debug("Initial pts: {}", cowardPts);
            }
            setAttribute(COWARD_PTS, cowardPts);
        });
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(TickEvent.TYPE) && target.as(PLAYER_OBJ).map(p -> p.getStat(HP)).orElse(0) > 10) {
            if (getAttribute(COWARD_PTS).orElse(0) < MAX_COWARD) {
                changeAttribute(COWARD_PTS, 1);
            }
            log.debug("Current coward pts: {}", getAttribute(COWARD_PTS).orElse(0));
        }
        return super.processEvent(event);
    }

    @Override
    public Optional<String> getDescription(Verbosity verbosity) {
        if (verbosity == Verbosity.SHORT) {
            return Optional.of("ссыкло [" + getAttribute(COWARD_PTS).orElse(0) + "]");
        }
        if (verbosity == Verbosity.VERBOSE) {
            return Optional.of("Вы так боитесь идти сражаться, что сидите на одном месте и обшарили практически всё вокруг. "
                    + "Шанс найти что-либо становится всё ниже и ниже.");
        }
        return super.getDescription(verbosity);
    }

    @Override
    public void onAfterDetach() {
        target.log("Вы больше не ссыкло!");
    }

}
