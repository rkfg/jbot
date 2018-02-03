package me.rkfg.xmpp.bot.message;

import me.rkfg.xmpp.bot.matrix.StateManager;

public class MatrixMessage implements Message {

    private StateManager displayNames;
    private String fromRoom;
    private String from;
    private String body;

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
        return getFormattedFrom() + ": ";
    }

    public String getFormattedFrom() {
        return "<a href=\"https://matrix.to/#/" + from + "\">" + getNick() + "</a>";
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
}
