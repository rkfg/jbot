package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.BattleFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.CowardEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StaminaRegenEffect;

public class BattleEvent extends AbstractEvent {

    private static final int ROLL_LIMIT = 10000;
    public static final String TYPE = "battle";

    public BattleEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
        setAttribute(BattleFatigueEffect.FAIR, true);
    }

    @Override
    public void apply() {
        source.as(PLAYER_OBJ).ifPresent(attacker -> target.as(PLAYER_OBJ).ifPresent(defender -> {
            final boolean attackInviteSucceeded = attacker.enqueueEvent(createInviteEvent(attacker, defender));
            final boolean defendInviteSucceeded = defender.enqueueEvent(createInviteEvent(attacker, defender));
            if (attackInviteSucceeded) {
                attacker.getEffect(StaminaRegenEffect.TYPE).ifPresent(e -> e.setAttribute(StaminaRegenEffect.IDLE, 0));
                attacker.enqueueDetachEffect(CowardEffect.TYPE);
            }
            if (!attackInviteSucceeded || !defendInviteSucceeded) {
                return;
            }
            attacker.enqueueEvent(new BattleBeginsEvent(attacker, defender));
            defender.enqueueEvent(new BattleBeginsEvent(attacker, defender));
            String[] keys = new String[] { "%atk%", "%def%", "%wpn%", "%prt%" };
            String[] vals = new String[] { attacker.getName(), defender.getName(), attacker.getWeaponName(), defender.getArmorName() };
            attacker.log("atkb", keys, vals);
            defender.log("defb", keys, vals);
            BattleAttackEvent attackEvent = null;
            BattleAttackEvent defenceEvent = null;
            int rollLimit = ROLL_LIMIT;
            // roll until at least one attack succeeds
            while (rollLimit-- > 0 && isRollUnsuccessful(attackEvent, defenceEvent)) {
                attackEvent = new BattleAttackEvent(attacker, defender);
                defenceEvent = new BattleAttackEvent(defender, attacker);
            }
            battleTurn(attackEvent);
            battleTurn(defenceEvent);
            attacker.enqueueEvent(new BattleEndsEvent(attacker, defender));
            defender.enqueueEvent(new BattleEndsEvent(attacker, defender));
            if (attacker.isAlive() && defender.isAlive()) {
                attacker.log("bend", keys, vals);
                defender.log("bend", keys, vals);
            }
            attacker.dumpStats();
            defender.dumpStats();
        }));
    }

    private boolean isRollUnsuccessful(BattleAttackEvent attackEvent, BattleAttackEvent defenceEvent) {
        return (attackEvent == null || !attackEvent.isSuccessful()) && (defenceEvent == null || !defenceEvent.isSuccessful());
    }

    public BattleInviteEvent createInviteEvent(IPlayer attacker, IPlayer defender) {
        final BattleInviteEvent event = new BattleInviteEvent(attacker, defender);
        getAttribute(BattleFatigueEffect.FAIR).ifPresent(f -> event.setAttribute(BattleFatigueEffect.FAIR, f));
        return event;
    }

    private void battleTurn(BattleAttackEvent attackEvent) {
        attackEvent.getSource().as(PLAYER_OBJ).ifPresent(attacker -> attackEvent.getTarget().as(PLAYER_OBJ).ifPresent(defender -> {
            if (defender.enqueueEvent(attackEvent)
                    && defender.as(PLAYER_OBJ).flatMap(IPlayer::getArmor).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                    && attacker.as(PLAYER_OBJ).flatMap(IPlayer::getWeapon).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                    && attacker.enqueueEvent(attackEvent)) {
                String[] keys = new String[] { "%atk%", "%def%", "%wpn%", "%prt%", "%hp%" };
                String[] vals = new String[] { attacker.getName(), defender.getName(), "<b>" + attacker.getWeaponName() + "</b>",
                        "<b>" + defender.getArmorName() + "</b>", "<b><red>" + attackEvent.getDamage() + "</red></b>" };
                if (attackEvent.isSuccessful()) {
                    if (attackEvent.getDamage() >= 10) {
                        // CRITICAL HIT
                        attacker.log("atksc", keys, vals, m -> "<_dblue><green>*%* " + m.toUpperCase() + " *%*</green></_dblue>");
                        defender.log("deffc", keys, vals, m -> "<_purple><yellow>@&@ " + m.toUpperCase() + " @&@</yellow></_purple>");
                    } else {
                        attacker.log("atks", keys, vals);
                        defender.log("deff", keys, vals);
                    }
                    final StatsEvent statsEvent = new StatsEvent();
                    statsEvent.setSource(attacker);
                    defender.enqueueEvent(statsEvent.setAttributeChain(HP, -attackEvent.getDamage()));
                } else {
                    attacker.log("atkf", keys, vals);
                    defender.log("defs", keys, vals);
                }
            }
        }));
    }

}
