package me.rkfg.xmpp.bot.xmpp;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import me.rkfg.xmpp.bot.Main;

public class ChatAdapterImpl extends LoggingChatAdapter {
    private Chat chat;

    public ChatAdapterImpl(Chat chat) {
        super(Main.INSTANCE.getBotNick() + " ⇒ " + chat.getParticipant());
        this.chat = chat;
    }

    @Override
    public void sendActualMessage(String message) throws XMPPException, NotConnectedException {
        chat.sendMessage(message);
    }

    @Override
    public void sendActualMessage(Message message) throws XMPPException, NotConnectedException {
        chat.sendMessage(message);
    }
}
