package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public class BattleEndsEvent extends AbstractEvent {

    private static final String TYPE = "battleends";

    public BattleEndsEvent(IGameObject source, String battleComment) {
        super(TYPE, source);
        setComment(battleComment + " завершён.");
    }

}