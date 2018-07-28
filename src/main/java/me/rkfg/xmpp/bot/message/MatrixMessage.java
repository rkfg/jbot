package me.rkfg.xmpp.bot.message;

import me.rkfg.xmpp.bot.matrix.StateManager;

public class MatrixMessage implements BotMessage {

    private StateManager displayNames;
    private String fromRoom;
    private String from;
    private String body;
    private int retryCount = 3;
    private long resendTS = 0;

    public MatrixMessage(StateManager displayNames, String body, String fromRoom, String from) {
        this.displayNames = displayNames;
        this.fromRoom = fromRoom;
        this.from = from;
        this.body = body;
    }

    @Override
    public boolean isFromUser() {
        return true;
    }

    @Override
    public String getNick() {
        return displayNames.getName(from);
    }

    @Override
    public String getAppeal(String target) {
        return getFormattedFrom(target) + ": ";
    }

    public String getFormattedFrom(String nick) {
        return "<a href=\"https://matrix.to/#/" + from + "\">" + nick + "</a>";
    }

    @Override
    public boolean isFromGroupchat() {
        return true;
    }

    @Override
    public String getAppeal() {
        return getAppeal(getNick());
    }

    @Override
    public String getFromRoom() {
        return fromRoom;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.MATRIX;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void decRetry() {
        retryCount--;
    }

    public long getResendTS() {
        return resendTS;
    }

    public void setResendTS(long resendTS) {
        this.resendTS = resendTS;
    }

    public void addResendTS(long shift) {
        resendTS += shift;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MatrixMessage getOriginalMessage() {
        return this;
    }
}
