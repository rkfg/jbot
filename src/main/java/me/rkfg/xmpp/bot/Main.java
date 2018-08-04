package me.rkfg.xmpp.bot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.irc.IRCBot;
import me.rkfg.xmpp.bot.matrix.MatrixBot;
import me.rkfg.xmpp.bot.xmpp.XMPPBot;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.SettingsManager;

public class Main {

    public static final IBot INSTANCE;
    private static Logger log = LoggerFactory.getLogger(Main.class);

    static {
        SettingsManager sm = SettingsManager.getInstance();
        sm.setFilename("settings.ini");
        try {
            sm.loadSettings();
        } catch (IOException e) {
            log.warn("{}", e);
        }
        String type = sm.getStringSetting("type");
        if (type == null) {
            INSTANCE = null;
        } else {
            switch (type) {
            case "xmpp":
                INSTANCE = new XMPPBot();
                break;
            case "matrix":
                INSTANCE = new MatrixBot();
                break;
            case "irc":
                INSTANCE = new IRCBot();
                break;
            default:
                INSTANCE = null;
                break;
            }
        }
    }

    public static void main(String[] args) throws LogicException {
        if (INSTANCE == null) {
            log.error("Set 'type' parameter in settings.ini to the bot type (xmpp, matrix or irc).");
            System.exit(-1);
        }
        System.exit(INSTANCE.run());
    }

}
