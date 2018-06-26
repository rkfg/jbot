package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.Player;

public class TickEvent extends AbstractEvent {

    public static final String TICK_EVENT_TYPE = "tiptop";

    public TickEvent() {
        super(TICK_EVENT_TYPE, Player.WORLD);
    }

    @Override
    public void apply() {
        source.log("Tick-tock");
    }

}
