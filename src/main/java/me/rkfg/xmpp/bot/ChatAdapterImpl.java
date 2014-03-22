package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class ChatAdapterImpl implements ChatAdapter {
    private Chat chat;

    public ChatAdapterImpl(Chat chat) {
        this.chat = chat;
    }

    public void sendMessage(String message) throws XMPPException, NotConnectedException {
        chat.sendMessage(message);
    }

    public void sendMessage(Message message) throws XMPPException, NotConnectedException {
        chat.sendMessage(message);
    }
}
