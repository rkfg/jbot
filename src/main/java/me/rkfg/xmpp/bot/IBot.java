package me.rkfg.xmpp.bot;

import java.util.List;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.SettingsManager;

public interface IBot {

    enum Protocol {
        XMPP, MATRIX, IRC
    }

    int run() throws LogicException;

    SettingsManager getSettingsManager();

    List<MessagePlugin> getPlugins();

    String getBotNick();

    String sendMessage(String message, String roomName);

    void sendMessage(String message);
    
    Set<String> getRoomsWithUser(String userId);

    boolean isDirectChat(String roomId);
    
    Protocol getProtocol();

}
