package me.rkfg.xmpp.bot.plugins.game;

public interface IPlayer extends IGameObject {

    boolean isAlive();

    String getId();

    String getName();

    void dumpStats();

    String getLog();

}
