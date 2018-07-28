package me.rkfg.xmpp.bot.message;

public interface BotMessage {

    public enum Protocol {
        XMPP, MATRIX, IRC
    }

    boolean isFromUser();

    String getNick();

    String getAppeal(String target);

    boolean isFromGroupchat();

    default String getAppeal() {
        return getAppeal(getNick());
    }

    String getFromRoom();

    String getFrom();

    String getBody();

    Protocol getProtocol();

    <T> T getOriginalMessage();

}
