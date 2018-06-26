package me.rkfg.xmpp.bot.plugins.game.effect;

public interface IAttachDetachEffect {

    void attachEffect(IEffect effect);

    void detachEffect(String name);
    
}
