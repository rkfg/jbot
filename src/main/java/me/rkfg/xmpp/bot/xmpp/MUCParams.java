package me.rkfg.xmpp.bot.xmpp;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

public class MUCParams {
    private MessageListener messageListener;
    private ParticipantStatusListener participantStatusListener;
    private ChatAdapter mucAdapted;

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ParticipantStatusListener getParticipantStatusListener() {
        return participantStatusListener;
    }

    public void setParticipantStatusListener(ParticipantStatusListener participantStatusListener) {
        this.participantStatusListener = participantStatusListener;
    }

    public ChatAdapter getMucAdapted() {
        return mucAdapted;
    }

    public void setMucAdapted(ChatAdapter mucAdapted) {
        this.mucAdapted = mucAdapted;
    }

}
