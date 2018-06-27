package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;
import java.util.Collections;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;

public interface IEffect extends IHasAttributes, IHasType {

    default Collection<IEvent> processEvent(IEvent event) {
        return Collections.emptySet();
    }

    default void onAttach() {

    }

    default void onDetach() {

    }

    IGameObject getSource();

    void setTarget(IGameObject target);

    IGameObject getTarget();

    String getLocalizedName();

    default boolean isReplacementAllowed(IEffect replacement) {
        return true;
    }

}
