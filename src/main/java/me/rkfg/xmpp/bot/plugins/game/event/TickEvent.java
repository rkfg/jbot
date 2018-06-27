package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.Player;

public class TickEvent extends AbstractEvent {

    public static final String TYPE = "tiptop";

    public TickEvent() {
        super(TYPE, Player.WORLD);
    }

}
