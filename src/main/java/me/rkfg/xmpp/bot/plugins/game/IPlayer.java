package me.rkfg.xmpp.bot.plugins.game;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IPlayer extends IGameObject {

    boolean isAlive();

    String getId();

    String getName();

    void dumpStats();

    String getLog();

    Integer getStat(TypedAttribute<Integer> attr);

}
