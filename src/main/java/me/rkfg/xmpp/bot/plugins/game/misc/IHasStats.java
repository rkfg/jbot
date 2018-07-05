package me.rkfg.xmpp.bot.plugins.game.misc;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public interface IHasStats extends IGameObject, IHasAttributes {

    default Integer getStat(TypedAttribute<Integer> attr) {
        return getAttrs().get(attr).orElse(0);
    }
}
