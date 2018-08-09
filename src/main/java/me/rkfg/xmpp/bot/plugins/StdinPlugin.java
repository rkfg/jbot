package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.message.BotMessage;

public class StdinPlugin extends MessagePluginImpl {

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
        new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                while (!Thread.interrupted()) {
                    try {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            // stdin is not connected
                            break;
                        }
                        sendMessage(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Stdin handler").start();
    }

    @Override
    public String getManual() {
        return null;
    }

}
