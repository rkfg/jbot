package me.rkfg.xmpp.bot;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;

import me.rkfg.xmpp.bot.matrix.MatrixBot;
import ru.ppsrk.gwt.client.LogicException;

public class Main {

    public static final IBot INSTANCE = new MatrixBot();

    public static void main(String[] args) throws InterruptedException, SmackException, IOException, LogicException {
        INSTANCE.run();
    }

}
