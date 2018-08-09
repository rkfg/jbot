package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.message.BotMessage;

public class GoodbyePlugin extends MessagePluginImpl {

    @Override
    public Pattern getPattern() {
        return null;
    }

    @Override
    public String process(BotMessage message, Matcher matcher) {
        return null;
    }

    @Override
    public void init() {
        super.init();
        getSettingsManager().setDefault("goodbye_msg", "Я всё.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                sendMessage(getSettingsManager().getStringSetting("goodbye_msg"));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
