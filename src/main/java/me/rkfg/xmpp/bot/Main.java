package me.rkfg.xmpp.bot;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;

public class Main {

    public static void main(String[] args) throws InterruptedException, SmackException, IOException {
        Bot.INSTANCE.run();
    }

}
