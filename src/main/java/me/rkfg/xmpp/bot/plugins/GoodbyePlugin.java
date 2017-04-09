package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.packet.Message;

public class GoodbyePlugin extends MessagePluginImpl {

    @Override
    public Pattern getPattern() {
        return null;
    }

    @Override
    public String process(Message message, Matcher matcher) {
        return null;
    }

    @Override
    public void init() {
        super.init();
        getSettingsManager().setDefault("goodbye_msg", "Я всё.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                sendMUCMessage(getSettingsManager().getStringSetting("goodbye_msg"));
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
