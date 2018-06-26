package me.rkfg.xmpp.bot.plugins.game;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IMutablePlayer extends IPlayer {

    void setId(String id);

    void setName(String name);

    void changeStat(TypedAttribute<Integer> attr, Integer diff);

}
