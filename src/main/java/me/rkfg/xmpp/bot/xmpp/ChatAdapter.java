package me.rkfg.xmpp.bot.xmpp;

import me.rkfg.xmpp.bot.message.BotMessage;

public interface ChatAdapter {
    public void sendMessage(String message);

    public void sendMessage(BotMessage message);
}
