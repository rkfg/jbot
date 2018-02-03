package me.rkfg.xmpp.bot;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;

import me.rkfg.xmpp.bot.matrix.MatrixBot;

public class Main {

    public static IBot INSTANCE = new MatrixBot();

    public static void main(String[] args) throws InterruptedException, SmackException, IOException {
        INSTANCE.run();
    }

}
