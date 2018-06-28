package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class BattleBeginsEvent extends AbstractEvent {

    public static final String TYPE = "battlebegins";

    public BattleBeginsEvent(IGameObject source) {
        super(TYPE, source);
    }

    @Override
    public void apply() {
        if (target == source) { // this is us, not starting battle just yet
            return;
        }
        // enemy agreed to start the battle
        final String baseBattleComment = "Бой между " + Utils.getPlayerName(source) + " и " + Utils.getPlayerName(target);
        setComment(baseBattleComment + " начинается.");
        logTargetComment();
        logSourceComment();
        battleTurn(source, target);
        battleTurn(target, source);
        source.enqueueEvent(new BattleEndsEvent(source, baseBattleComment));
        target.enqueueEvent(new BattleEndsEvent(source, baseBattleComment));
    }

    private void battleTurn(IGameObject srcPlayer, IGameObject tgtPlayer) {
        AttackEvent attackEvent = new AttackEvent(srcPlayer, tgtPlayer);
        if (tgtPlayer.enqueueEvent(attackEvent) && srcPlayer.enqueueEvent(attackEvent)) {
            if (attackEvent.isSuccessful()) {
                srcPlayer.log("Атака достигает цели и наносит " + attackEvent.getDamage() + " урона!");
                tgtPlayer.log("Соперник наносит вам " + attackEvent.getDamage() + " урона!");
                tgtPlayer.enqueueEvent(new StatsEvent(srcPlayer).setAttributeChain(HP, -attackEvent.getDamage()));
            } else {
                srcPlayer.log("Вы пытаетесь поразить соперника, но промахиваетесь!");
                tgtPlayer.log("Соперник пытается нанести удар, но промахивается!");
            }
        }
    }

}
