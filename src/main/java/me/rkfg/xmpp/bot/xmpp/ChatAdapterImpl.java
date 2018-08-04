package me.rkfg.xmpp.bot.xmpp;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.BotMessage;

public class ChatAdapterImpl extends LoggingChatAdapter {
    private Chat chat;

    public ChatAdapterImpl(Chat chat) {
        super(Main.INSTANCE.getBotNick() + " ⇒ " + chat.getParticipant());
        this.chat = chat;
    }

    @Override
    public void sendActualMessage(String message) {
        try {
            chat.sendMessage(message);
        } catch (NotConnectedException e) {
            throw new XMPPBotException(e);
        }
    }

    @Override
    public void sendActualMessage(BotMessage message) {
        try {
            chat.sendMessage((Message) message.getOriginalMessage());
        } catch (NotConnectedException e) {
            throw new XMPPBotException(e);
        }
    }
}
