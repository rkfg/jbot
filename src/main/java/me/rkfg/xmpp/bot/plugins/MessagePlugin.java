package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.message.Message;

public interface MessagePlugin {
    public Pattern getPattern();

    public String process(Message message, Matcher matcher);

    public void init();

    public String getManual();

}
