package me.rkfg.xmpp.bot.plugins.game.misc;

public interface IHasType {

    public String getType();

    default boolean isOfType(String type) {
        return getType().equals(type);
    }
}
