package me.rkfg.xmpp.bot.plugins.game.effect;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;

public interface IAttachDetachEffect extends IGameBase {

    void attachEffect(IEffect effect);

    void detachEffect(String type);

}
