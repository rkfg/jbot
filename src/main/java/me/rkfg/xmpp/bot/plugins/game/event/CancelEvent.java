package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.World;

public class CancelEvent extends AbstractEvent {

    public static final String TYPE = "cancel";

    public CancelEvent() {
        super(TYPE, World.THIS);
    }

    @Override
    public void apply() {
        // just a marker event, doesn't do anything
    }

}
