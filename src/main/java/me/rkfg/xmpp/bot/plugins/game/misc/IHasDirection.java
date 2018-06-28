package me.rkfg.xmpp.bot.plugins.game.misc;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;
import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public interface IHasDirection extends IGameBase {

    public IGameObject getSource();

    public void setSource(IGameObject source);

    public IGameObject getTarget();

    public void setTarget(IGameObject target);

}
