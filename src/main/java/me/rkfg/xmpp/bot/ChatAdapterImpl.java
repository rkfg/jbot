package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class ChatAdapterImpl extends LoggingChatAdapter {
    private Chat chat;

    public ChatAdapterImpl(Chat chat) {
        super(Main.getNick() + " ⇒ " + chat.getParticipant());
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
