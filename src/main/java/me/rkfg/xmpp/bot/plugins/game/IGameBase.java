package me.rkfg.xmpp.bot.plugins.game;

import java.util.Optional;

public interface IGameBase {
    default Optional<String> getDescription() {
        return Optional.empty();
    }
}
