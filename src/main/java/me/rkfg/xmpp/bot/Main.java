package me.rkfg.xmpp.bot;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;

public class Main {

    public static Bot INSTANCE = new Bot();
    
    public static void main(String[] args) throws InterruptedException, SmackException, IOException {
        INSTANCE.run();
    }

}
