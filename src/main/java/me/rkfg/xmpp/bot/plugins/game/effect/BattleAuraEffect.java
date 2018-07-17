package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class BattleAuraEffect extends AbstractEffect implements IBattleEffect {

    public class OmegaBeatEffect extends StatsEffect {
        public static final String TYPE = "omegaeff";

        public OmegaBeatEffect() {
            super(OmegaBeatEffect.TYPE, "эффект омежки");
            setStatChange(DEF, 2);
        }
    }

    private static final String OMEGA_TRAIT = "omega";
    private static final String ALPHA_TRAIT = "alpha";
    public static final String TYPE = "battleaura";

    private class AlphaBeatEffect extends StatsEffect {
        public static final String TYPE = "alphaeff";

        public AlphaBeatEffect() {
            super(AlphaBeatEffect.TYPE, "эффект альфача");
            setStatChange(ATK, 2);
        }
    }

    public BattleAuraEffect() {
        super(TYPE, "обработчик аур");
    }

    @Override
    public Collection<IEvent> battleBegins(IEvent event) {
        return withPlayers(event, (attacker, defender) -> {
            if (attacker == target) {
                if (attacker.hasTrait(ALPHA_TRAIT) && defender.hasTrait(OMEGA_TRAIT)) {
                    attacker.log("Противник оказался омежкой, и вас наполняет энтузиазм.");
                    attacker.enqueueAttachEffect(new AlphaBeatEffect());
                }
                if (attacker.hasTrait(OMEGA_TRAIT) && !defender.hasTrait(ALPHA_TRAIT) && !defender.hasTrait(OMEGA_TRAIT)) {
                    attacker.log("Годы унижений не прошли даром, и вы видите атаки противника на два шага вперёд.");
                    attacker.enqueueAttachEffect(new OmegaBeatEffect());
                }
            }
            return noEvent();
        });
    }

    @Override
    public Collection<IEvent> battleEnds(IEvent event) {
        return withPlayers(event, (attacker, defender) -> {
            attacker.enqueueDetachEffect(AlphaBeatEffect.TYPE);
            attacker.enqueueDetachEffect(OmegaBeatEffect.TYPE);
            return noEvent();
        });
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
