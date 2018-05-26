package me.rkfg.xmpp.bot.message;

import org.jivesoftware.smack.packet.Message.Type;
import org.jxmpp.util.XmppStringUtils;

public class XMPPMessage implements Message {
    private org.jivesoftware.smack.packet.Message msg;

    public XMPPMessage(org.jivesoftware.smack.packet.Message msg) {
        this.msg = msg;
    }

    @Override
    public String getNick() {
        return XmppStringUtils.parseResource(msg.getFrom());
    }

    @Override
    public boolean isFromUser() {
        return !getNick().isEmpty();
    }

    @Override
    public String getAppeal(String target) {
        return isFromGroupchat() ? target + ", " : "";
    }

    @Override
    public boolean isFromGroupchat() {
        return msg.getType() == Type.groupchat;
    }

    @Override
    public String getAppeal() {
        return getAppeal(getNick());
    }

    @Override
    public String getFromRoom() {
        return XmppStringUtils.parseBareJid(msg.getFrom());
    }

    @Override
    public String getFrom() {
        return msg.getFrom();
    }

    @Override
    public String getBody() {
        return msg.getBody();
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.XMPP;
    }

}
