package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class BattleEvent extends AbstractEvent {

    public static final String TYPE = "battle";

    public BattleEvent(IPlayer attacker, IPlayer defender) {
        super(TYPE);
        setSource(attacker);
        setTarget(defender);
    }

    @Override
    public void apply() {
        source.as(PLAYER_OBJ).ifPresent(attacker -> target.as(PLAYER_OBJ).ifPresent(defender -> {
            if (!attacker.enqueueEvent(new BattleBeginsEvent(attacker, defender))
                    || !defender.enqueueEvent(new BattleBeginsEvent(attacker, defender))) {
                attacker.log("Не удалось начать бой");
                return;
            }
            attacker.log(String.format("Вы нападаете на %s!", Utils.getPlayerName(defender)));
            defender.log(String.format("%s нападает на вас!", Utils.getPlayerName(attacker)));
            battleTurn(attacker, defender);
            battleTurn(defender, attacker);
            attacker.enqueueEvent(new BattleEndsEvent(attacker, defender));
            defender.enqueueEvent(new BattleEndsEvent(attacker, defender));
            String endMessage = String.format("Бой между %s и %s завершён.", Utils.getPlayerName(attacker), Utils.getPlayerName(defender));
            attacker.log(endMessage);
            defender.log(endMessage);
        }));
    }

    private void battleTurn(IGameObject srcPlayer, IGameObject tgtPlayer) {
        BattleAttackEvent attackEvent = new BattleAttackEvent(srcPlayer, tgtPlayer);
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
    }

}
