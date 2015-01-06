package me.rkfg.xmpp.bot.plugins;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessagePluginImpl implements MessagePlugin {
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected final String getNick(Message message) {
        return StringUtils.parseResource(message.getFrom());
    }

    protected final String getBareAddress(Message message) {
        return StringUtils.parseBareAddress(message.getFrom());
    }

    protected boolean isMessageFromUser(Message message) {
        return !getNick(message).isEmpty();
    }

    protected String getAppeal(Message message, String target) {
        return message.getType() == Type.groupchat ? target + ", " : "";
    }

    protected String getAppeal(Message message) {
        return getAppeal(message, StringUtils.parseResource(message.getFrom()));
    }

    @Override
    public void init() {
        // do nothing by default
    }

    @Override
    public String getManual() {
        return "справка по плагину отсутствует.";
    }
}
