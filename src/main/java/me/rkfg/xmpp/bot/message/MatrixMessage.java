package me.rkfg.xmpp.bot.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.rkfg.xmpp.bot.IBot;
import me.rkfg.xmpp.bot.matrix.StateManager;

public class MatrixMessage implements BotMessage {

    private static final Map<String, String> REPLACES = new HashMap<>();
    private StateManager displayNames;
    private String fromRoom;
    private String from;
    private String body;
    private int retryCount = 3;
    private long resendTS = 0;

    static {
        REPLACES.put("white", "FFFFFF");
        REPLACES.put("black", "000000");
        REPLACES.put("dblue", "00007F");
        REPLACES.put("dgreen", "009300");
        REPLACES.put("red", "FF0000");
        REPLACES.put("brown", "7F0000");
        REPLACES.put("purple", "9C009C");
        REPLACES.put("olive", "FC7F00");
        REPLACES.put("yellow", "FFFF00");
        REPLACES.put("green", "00FC00");
        REPLACES.put("teal", "009393");
        REPLACES.put("cyan", "00FFFF");
        REPLACES.put("blue", "0000FC");
        REPLACES.put("magenta", "FF00FF");
        REPLACES.put("dgray", "7F7F7F");
        REPLACES.put("gray", "D2D2D2");
    }

    public MatrixMessage(StateManager displayNames, String body, String fromRoom, String from) {
        this.displayNames = displayNames;
        this.fromRoom = fromRoom;
        this.from = from;
        this.body = processTags(body);
    }

    private String processTags(String body) {
        for (Entry<String, String> entry : REPLACES.entrySet()) {
            body = body.replaceAll("<" + entry.getKey() + ">", "<font color=\"#" + entry.getValue() + "\">");
            body = body.replaceAll("<_" + entry.getKey() + ">", "<font data-mx-bg-color=\"#" + entry.getValue() + "\">");
            body = body.replaceAll("</" + entry.getKey() + ">", "</font>");
            body = body.replaceAll("</_" + entry.getKey() + ">", "</font>");
        }
        return body;
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
    public IBot.Protocol getProtocol() {
        return IBot.Protocol.MATRIX;
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
