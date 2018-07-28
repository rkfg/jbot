package me.rkfg.xmpp.bot;

import me.rkfg.xmpp.bot.irc.IRCBot;
import ru.ppsrk.gwt.client.LogicException;

public class Main {

    public static final IBot INSTANCE = new IRCBot();

    public static void main(String[] args) throws LogicException {
        INSTANCE.run();
    }

}
