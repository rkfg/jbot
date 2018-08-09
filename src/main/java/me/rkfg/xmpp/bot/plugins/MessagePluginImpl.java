package me.rkfg.xmpp.bot.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.Main;
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

    protected SettingsManager getSettingsManager() {
        return Main.INSTANCE.getSettingsManager();
    }

    protected void sendMessage(String message, String roomName) {
        Main.INSTANCE.sendMessage(message, roomName);
    }

    protected void sendMessage(String message) {
        Main.INSTANCE.sendMessage(message);
    }

    protected String getBotNick() {
        return Main.INSTANCE.getBotNick();
    }
}
