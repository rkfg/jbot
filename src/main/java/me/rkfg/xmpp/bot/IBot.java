package me.rkfg.xmpp.bot;

import java.util.List;

import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;

import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import me.rkfg.xmpp.bot.xmpp.ChatAdapter;
import me.rkfg.xmpp.bot.xmpp.MUCManager;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.SettingsManager;

public interface IBot {

    void run() throws LogicException;

    SettingsManager getSettingsManager();

    List<MessagePlugin> getPlugins();

    String getBotNick();

    void processMessage(ChatAdapter mucAdapted, Message message);

    String sendMessage(String message, String mucName);

    void sendMessage(String message);

    ChatManager getChatManagerInstance();

    MUCManager getMUCManager();

}
