package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.packet.Message;

public interface MessagePlugin {
    public Pattern getPattern();

    public String process(Message message, Matcher matcher);
}
