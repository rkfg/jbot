package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class MUCAdapterImpl extends LoggingChatAdapter {
    private MultiUserChat multiUserChat;

    public MUCAdapterImpl(MultiUserChat multiUserChat) {
        super(multiUserChat.getRoom() + "/" + multiUserChat.getNickname());
        this.multiUserChat = multiUserChat;
    }

    public void sendActualMessage(String message) throws XMPPException, NotConnectedException {
        multiUserChat.sendMessage(message);
    }

    public void sendActualMessage(Message message) throws XMPPException, NotConnectedException {
        multiUserChat.sendMessage(message);
    }
}
