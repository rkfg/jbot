package me.rkfg.xmpp.bot.xmpp;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggingChatAdapter implements ChatAdapter {

    private Logger log = LoggerFactory.getLogger(getClass());
    private String from;

    protected LoggingChatAdapter(String from) {
        this.from = from;
    }

    private void logMessage(String from, String message) {
        log.info("<{}>: {}", from, message);
    }

    public void sendMessage(String message) throws XMPPException, NotConnectedException {
        logMessage(from, message);
        sendActualMessage(message);
    }

    public void sendMessage(Message message) throws XMPPException, NotConnectedException {
        logMessage(from, message.getBody());
        sendActualMessage(message);
    }

    protected abstract void sendActualMessage(String message) throws XMPPException, NotConnectedException;

    protected abstract void sendActualMessage(Message message) throws XMPPException, NotConnectedException;
}
