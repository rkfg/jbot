package me.rkfg.xmpp.bot.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.Main;

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
        Main.getSettingsManager().getDefaults().put("goodbye_msg", "Я всё.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                Main.sendMUCMessage(Main.getSettingsManager().getStringSetting("goodbye_msg"));
            }
        }));
    }
}
