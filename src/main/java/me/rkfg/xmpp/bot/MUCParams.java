package me.rkfg.xmpp.bot;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

public class MUCParams {
    private PacketListener messageListener;
    private ParticipantStatusListener participantStatusListener;
    private ChatAdapter mucAdapted;

    public PacketListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(PacketListener messageListener) {
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
