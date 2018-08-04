package me.rkfg.xmpp.bot.message;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jxmpp.util.XmppStringUtils;

import me.rkfg.xmpp.bot.IBot;

public class XMPPMessage implements BotMessage {
    private Message msg;

    public XMPPMessage(Message msg) {
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
    public IBot.Protocol getProtocol() {
        return IBot.Protocol.XMPP;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Message getOriginalMessage() {
        return msg;
    }

}
