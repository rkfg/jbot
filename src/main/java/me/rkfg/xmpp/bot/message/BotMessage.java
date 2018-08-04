package me.rkfg.xmpp.bot.message;

import me.rkfg.xmpp.bot.IBot;

public interface BotMessage {

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

    IBot.Protocol getProtocol();

    <T> T getOriginalMessage();

}
