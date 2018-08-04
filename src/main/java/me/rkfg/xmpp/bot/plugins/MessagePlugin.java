package me.rkfg.xmpp.bot.plugins;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import me.rkfg.xmpp.bot.IBot;
import me.rkfg.xmpp.bot.message.BotMessage;

public interface MessagePlugin {

    public Pattern getPattern();

    public String process(BotMessage message, Matcher matcher);

    public void init();

    public String getManual();

    default Set<IBot.Protocol> getCompatibleProtocols() {
        return Sets.newHashSet(IBot.Protocol.XMPP, IBot.Protocol.MATRIX, IBot.Protocol.IRC);
    }

}
