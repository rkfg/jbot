package me.rkfg.xmpp.bot.plugins;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessagePluginImpl implements MessagePlugin {
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected String getNick(Message message) {
        return StringUtils.parseResource(message.getFrom());
    }

    protected boolean isMessageFromUser(Message message) {
        return !getNick(message).isEmpty();
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
