package me.rkfg.xmpp.bot.xmpp;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import me.rkfg.xmpp.bot.message.BotMessage;

public class MUCAdapterImpl extends LoggingChatAdapter {
    private MultiUserChat multiUserChat;

    public MUCAdapterImpl(MultiUserChat multiUserChat) {
        super(multiUserChat.getRoom() + "/" + multiUserChat.getNickname());
        this.multiUserChat = multiUserChat;
    }

    @Override
    public void sendActualMessage(String message) {
        try {
            multiUserChat.sendMessage(message);
        } catch (NotConnectedException e) {
            throw new XMPPBotException(e);
        }
    }

    @Override
    public void sendActualMessage(BotMessage message) {
        try {
            multiUserChat.sendMessage((Message) message.getOriginalMessage());
        } catch (NotConnectedException e) {
            throw new XMPPBotException(e);
        }
    }
}
