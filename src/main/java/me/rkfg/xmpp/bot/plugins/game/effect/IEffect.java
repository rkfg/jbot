package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;

public interface IEffect extends IHasAttributes {

    String getName();

    Collection<IEvent> processEvent(IEvent event);

    void onAttach();

    void onDetach();

    IGameObject getSource();

    void setTarget(IGameObject target);

    IGameObject getTarget();

    String getLocalizedName();

    boolean isReplacementAllowed(IEffect replacement);

}
