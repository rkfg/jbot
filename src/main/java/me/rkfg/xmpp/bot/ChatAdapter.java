package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public interface ChatAdapter {
    public void sendMessage(String message) throws XMPPException, NotConnectedException;

    public void sendMessage(Message message) throws XMPPException, NotConnectedException;
}
