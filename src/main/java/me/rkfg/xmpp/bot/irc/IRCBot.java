package me.rkfg.xmpp.bot.irc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import me.rkfg.xmpp.bot.BotBase;
import me.rkfg.xmpp.bot.IBot;
import me.rkfg.xmpp.bot.message.IRCMessage;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import ru.ppsrk.gwt.client.LogicException;

public class IRCBot extends BotBase implements IBot {

    private PircBotX bot;

    private int maxLength;

    private class Listener extends ListenerAdapter {
        @Override
        public void onMessage(MessageEvent event) throws Exception {
            processMessage(new IRCMessage(event));
        }

        @Override
        public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
            processMessage(new IRCMessage(event));
        }

        @Override
        public void onConnect(ConnectEvent event) throws Exception {
            String modes = sm.getStringSetting("ircModes");
            if (modes != null && !modes.isEmpty()) {
                bot.sendIRC().mode(bot.getNick(), modes);
            }
        }

        private void processMessage(IRCMessage ircMessage) {
            String body = ircMessage.getBody();
            for (MessagePlugin plugin : plugins) {
                Pattern pattern = plugin.getPattern();
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(body);
                    if (matcher.find()) {
                        try {
                            String result = plugin.process(ircMessage, matcher);
                            if (result != null && !result.isEmpty()) {
                                postprocessMessage(result).forEach(l -> ircMessage.getOriginalMessage().respond(l));
                                break;
                            }
                        } catch (Exception e) {
                            log.warn("{}", e);
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void init() {
        super.init();
        MessageParser.init();
    }

    @Override
    public int run() throws LogicException {
        init();
        sm.setDefault("maxLine", "256");
        sm.setDefault("ircPort", "6667");
        sm.setDefault("ircSsl", "0");
        maxLength = sm.getIntegerSetting("maxLine");
        final Builder builder = new Configuration.Builder().setLogin(sm.getStringSetting("login")).setName(sm.getStringSetting("nick"))
                .setNickservPassword(sm.getStringSetting("password"))
                .addServer(sm.getStringSetting("ircServer"), sm.getIntegerSetting("ircPort"))
                .addAutoJoinChannel("#" + sm.getStringSetting("join")).addListener(new Listener()).setAutoReconnect(true)
                .setAutoReconnectDelay(5000).setMessageDelay(100);
        if ("1".equals(sm.getStringSetting("ircSsl"))) {
            builder.setSocketFactory(SSLSocketFactory.getDefault());
        }
        builder.setVersion("jbot by rkfg https://github.com/rkfg/jbot");
        builder.setRealName(builder.getVersion());
        Configuration conf = builder.buildConfiguration();
        while (!Thread.interrupted()) {
            try {
                bot = new PircBotX(conf);
                bot.startBot();
            } catch (IOException | IrcException e) {
                log.warn("{}", e);
                return 2;
            }
        }
        return 0;
    }

    @Override
    public String sendMessage(String message, String mucName) {
        postprocessMessage(message).forEach(msg -> bot.sendIRC().message(mucName, msg));
        return null;
    }

    @Override
    public void sendMessage(String message) {
        postprocessMessage(message).forEach(l -> bot.getUserBot().getChannels().forEach(c -> c.send().message(l)));
    }

    private List<String> postprocessMessage(String message) {
        return new MessageParser(message, maxLength).process();
    }

    @Override
    public Set<String> getRoomsWithUser(String userId) {
        return Collections.emptySet();
    }

    @Override
    public boolean isDirectChat(String roomId) {
        return !roomId.startsWith("#");
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.IRC;
    }

}
