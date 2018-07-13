package me.rkfg.xmpp.bot.plugins.game.misc;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

public interface IHasTraits extends IHasAttributes {

    default boolean hasTrait(String trait) {
        return getAttribute(TRAITS).map(s -> s.contains(trait)).orElse(false);
    }
}
