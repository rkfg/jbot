package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.message.BotMessage;

public class WhatisPlugin extends MessagePluginImpl {

    private GoogleCommandPlugin googleCommandPlugin = new GoogleCommandPlugin();

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^(че|чё|что|кто)\\s(такой|такое|такая)\\s*(.+?)\\??$", Pattern.DOTALL | Pattern.UNICODE_CASE
                | Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String process(BotMessage message, Matcher matcher) {
        if (matcher.groupCount() == 3) {
            return googleCommandPlugin.searchString(matcher.group(3));
        }
        return null;
    }

}
