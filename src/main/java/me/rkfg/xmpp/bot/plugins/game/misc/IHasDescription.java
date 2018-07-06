package me.rkfg.xmpp.bot.plugins.game.misc;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

public interface IHasDescription extends IHasAttributes {

    public enum Verbosity {
        SHORT, WITH_PARAMS, VERBOSE;
    }

    default Optional<String> getDescription() {
        return getDescription(Verbosity.SHORT);
    }

    default Optional<String> getDescription(Verbosity verbosity) {
        switch (verbosity) {
        case SHORT:
            return getAttribute(DESCRIPTION);
        case WITH_PARAMS:
            return getDescriptionWithParams();
        case VERBOSE:
            return getAttribute(DESCRIPTION_V);
        default:
            break;
        }
        return Optional.empty();
    }

    default Optional<String> getDescriptionWithParams() {
        return getDescription(Verbosity.SHORT);
    }

    default void setDescription(String description) {
        setDescription(Verbosity.SHORT, description);
    }

    default void setDescription(Verbosity s, String description) {
        switch (s) {
        case SHORT:
            setAttribute(DESCRIPTION, description);
            break;
        case VERBOSE:
            setAttribute(DESCRIPTION_V, description);
            break;
        default:
            break;
        }
    }
}
