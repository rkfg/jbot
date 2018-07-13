package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TraitEffect extends AbstractEffect {

    public static final String TYPE = "trait";

    public TraitEffect() {
        super(TYPE, "имеет особые свойства");
    }

    @Override
    public void onAfterAttach() {
        int i = 0;
        Optional<String> t;
        Set<String> traits = target.getAttribute(TRAITS).orElse(new HashSet<>());
        while ((t = getParameter(i++)).isPresent()) {
            traits.add(t.get());
        }
        target.setAttribute(TRAITS, traits);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
