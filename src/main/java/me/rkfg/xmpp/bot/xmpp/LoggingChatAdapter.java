package me.rkfg.xmpp.bot.xmpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.BotMessage;

public abstract class LoggingChatAdapter implements ChatAdapter {

    private Logger log = LoggerFactory.getLogger(getClass());
    private String from;

    protected LoggingChatAdapter(String from) {
        this.from = from;
    }

    private void logMessage(String from, String message) {
        log.info("<{}>: {}", from, message);
    }

    @Override
    public void sendMessage(String message) {
        logMessage(from, message);
        sendActualMessage(message);
    }

    @Override
    public void sendMessage(BotMessage message) {
        logMessage(from, message.getBody());
        sendActualMessage(message);
    }

    protected abstract void sendActualMessage(String message);

    protected abstract void sendActualMessage(BotMessage message);
}
