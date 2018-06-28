package me.rkfg.xmpp.bot.plugins.game.misc;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;

public interface IHasType extends IGameBase {

    public String getType();

    default boolean isOfType(String type) {
        return getType().equals(type);
    }
}
