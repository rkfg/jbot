package me.rkfg.xmpp.bot.message;

public interface Message {

    boolean isFromUser();

    String getNick();

    String getAppeal(String target);

    boolean isFromGroupchat();

    String getAppeal();

    String getFromRoom();

    String getFrom();

    String getBody();

}
