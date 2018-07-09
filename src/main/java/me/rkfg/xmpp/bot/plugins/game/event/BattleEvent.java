package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.BattleFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

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
            if (!attacker.enqueueEvent(createInviteEvent(attacker, defender))
                    || !defender.enqueueEvent(createInviteEvent(attacker, defender))) {
                return;
            }
            attacker.enqueueEvent(new BattleBeginsEvent(attacker, defender));
            defender.enqueueEvent(new BattleBeginsEvent(attacker, defender));
            attacker.log(String.format("Вы нападаете на %s!", Utils.getPlayerName(defender)));
            defender.log(String.format("%s нападает на вас!", Utils.getPlayerName(attacker)));
            BattleAttackEvent attackEvent = null;
            BattleAttackEvent defenceEvent = null;
            int rollLimit = ROLL_LIMIT;
            // roll until at least one attack succeeds
            while (rollLimit-- > 0 && (attackEvent == null || !attackEvent.isSuccessful()) && (defenceEvent == null || !defenceEvent.isSuccessful())) {
                attackEvent = new BattleAttackEvent(attacker, defender);
                defenceEvent = new BattleAttackEvent(defender, attacker);
            }
            battleTurn(attackEvent);
            battleTurn(defenceEvent);
            attacker.enqueueEvent(new BattleEndsEvent(attacker, defender));
            defender.enqueueEvent(new BattleEndsEvent(attacker, defender));
            String endMessage = String.format("Бой между %s и %s завершён.", Utils.getPlayerName(attacker), Utils.getPlayerName(defender));
            attacker.log(endMessage);
            defender.log(endMessage);
        }));
    }

    public BattleInviteEvent createInviteEvent(IPlayer attacker, IPlayer defender) {
        final BattleInviteEvent event = new BattleInviteEvent(attacker, defender);
        getAttribute(BattleFatigueEffect.FAIR).ifPresent(f -> event.setAttribute(BattleFatigueEffect.FAIR, f));
        return event;
    }

    private void battleTurn(BattleAttackEvent attackEvent) {
        attackEvent.getSource().as(PLAYER_OBJ).ifPresent(srcPlayer -> attackEvent.getTarget().as(PLAYER_OBJ).ifPresent(tgtPlayer -> {
            if (tgtPlayer.enqueueEvent(attackEvent)
                    && tgtPlayer.as(PLAYER_OBJ).flatMap(IPlayer::getArmor).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                    && srcPlayer.as(PLAYER_OBJ).flatMap(IPlayer::getWeapon).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                    && srcPlayer.enqueueEvent(attackEvent)) {
                if (attackEvent.isSuccessful()) {
                    srcPlayer.log("Атака достигает цели и наносит " + attackEvent.getDamage() + " урона!");
                    tgtPlayer.log("Соперник наносит вам " + attackEvent.getDamage() + " урона!");
                    final StatsEvent statsEvent = new StatsEvent();
                    statsEvent.setSource(srcPlayer);
                    tgtPlayer.enqueueEvent(statsEvent.setAttributeChain(HP, -attackEvent.getDamage()));
                } else {
                    srcPlayer.log("Вы пытаетесь поразить соперника, но промахиваетесь!");
                    tgtPlayer.log("Соперник пытается нанести удар, но промахивается!");
                }
            }
        }));
    }

}
