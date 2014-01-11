package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class MUCAdapterImpl implements ChatAdapter {
    private MultiUserChat multiUserChat;

    public MUCAdapterImpl(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
    }

    public void sendMessage(String message) throws XMPPException {
        multiUserChat.sendMessage(message);
    }

    public void sendMessage(Message message) throws XMPPException {
        multiUserChat.sendMessage(message);
    }
}
