package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;

public interface IEvent extends IHasAttributes {

    public void apply();

    public String getType();

    public void setTarget(IGameObject target);
}
