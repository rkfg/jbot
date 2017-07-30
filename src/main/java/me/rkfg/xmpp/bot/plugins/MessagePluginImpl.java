package me.rkfg.xmpp.bot.plugins;

import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.ChatAdapter;
import me.rkfg.xmpp.bot.MUCManager;
import me.rkfg.xmpp.bot.Main;
import ru.ppsrk.gwt.server.SettingsManager;

public abstract class MessagePluginImpl implements MessagePlugin {
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected final String getNick(String from) {
        return XmppStringUtils.parseResource(from);
    }

    protected final String getNick(Message message) {
        return XmppStringUtils.parseResource(message.getFrom());
    }

    protected final String getBareAddress(String from) {
        return XmppStringUtils.parseBareJid(from);
    }

    protected final String getBareAddress(Message message) {
        return XmppStringUtils.parseBareJid(message.getFrom());
    }

    protected boolean isMessageFromUser(Message message) {
        return !getNick(message).isEmpty();
    }

    protected String getAppeal(Message message, String target) {
        return isFromGroupchat(message) ? target + ", " : "";
    }

    protected boolean isFromGroupchat(Message message) {
        return message.getType() == Type.groupchat;
    }

    protected String getAppeal(Message message) {
        return getAppeal(message, getNick(message));
    }

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

    protected MUCManager getMUCManager() {
        return Main.INSTANCE.getMUCManager();
    }

    protected ChatManager getChatManagerInstance() {
        return Main.INSTANCE.getChatManagerInstance();
    }

    protected void sendMUCMessage(String message, String mucName) {
        Main.INSTANCE.sendMUCMessage(message, mucName);
    }

    protected void sendMessage(ChatAdapter chatAdapter, String message) {
        Main.INSTANCE.sendMessage(chatAdapter, message);
    }

    protected void sendMUCMessage(String message) {
        Main.INSTANCE.sendMUCMessage(message);
    }

    protected String getBotNick() {
        return Main.INSTANCE.getBotNick();
    }
}
