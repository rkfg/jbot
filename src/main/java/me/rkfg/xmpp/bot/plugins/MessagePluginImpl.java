package me.rkfg.xmpp.bot.plugins;

import org.jivesoftware.smack.chat.ChatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.xmpp.MUCManager;
import ru.ppsrk.gwt.server.SettingsManager;

public abstract class MessagePluginImpl implements MessagePlugin {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void init() {
        // do nothing by default
    }

    @Override
    public String getManual() {
        return "справка по плагину отсутствует.";
    }

    public String antiHighlight(String nick) {
        if (nick.length() < 2) {
            return nick;
        }
        return nick.substring(0, 1) + "\u200b" + nick.substring(1);
    }

    protected static SettingsManager getSettingsManager() {
        return Main.INSTANCE.getSettingsManager();
    }

    protected static MUCManager getMUCManager() {
        return Main.INSTANCE.getMUCManager();
    }

    protected static ChatManager getChatManagerInstance() {
        return Main.INSTANCE.getChatManagerInstance();
    }

    protected static void sendMUCMessage(String message, String mucName) {
        Main.INSTANCE.sendMessage(message, mucName);
    }

    protected static void sendMUCMessage(String message) {
        Main.INSTANCE.sendMessage(message);
    }

    protected static String getBotNick() {
        return Main.INSTANCE.getBotNick();
    }
}
