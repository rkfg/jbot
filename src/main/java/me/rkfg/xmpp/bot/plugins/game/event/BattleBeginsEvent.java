package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class BattleBeginsEvent extends AbstractEvent {

    public static final String TYPE = "battlebegins";

    public BattleBeginsEvent(IGameObject source) {
        super(TYPE);
        setSource(source);
    }

    @Override
    public void apply() {
        if (target == source) { // this is us, not starting battle just yet
            return;
        }
        // enemy agreed to start the battle
        final String baseBattleDescription = "Бой между " + Utils.getPlayerName(source) + " и " + Utils.getPlayerName(target);
        source.log(String.format("Вы нападаете на %s!", Utils.getPlayerName(target)));
        target.log(String.format("%s нападает на вас!", Utils.getPlayerName(source)));
        battleTurn(source, target);
        battleTurn(target, source);
        source.enqueueEvent(new BattleEndsEvent(source, baseBattleDescription));
        target.enqueueEvent(new BattleEndsEvent(source, baseBattleDescription));
    }

    private void battleTurn(IGameObject srcPlayer, IGameObject tgtPlayer) {
        AttackEvent attackEvent = new AttackEvent(srcPlayer, tgtPlayer);
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
