package me.rkfg.xmpp.bot.irc;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.chat.ChatManager;
import org.pircbotx.Colors;
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
import me.rkfg.xmpp.bot.exceptions.NotImplementedException;
import me.rkfg.xmpp.bot.message.BotMessage;
import me.rkfg.xmpp.bot.message.IRCMessage;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import me.rkfg.xmpp.bot.xmpp.ChatAdapter;
import me.rkfg.xmpp.bot.xmpp.MUCManager;
import ru.ppsrk.gwt.client.LogicException;

public class IRCBot extends BotBase implements IBot {

    private PircBotX bot;

    private static final Map<String, String> REPLACES = new HashMap<>();

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
                                ircMessage.getOriginalMessage().respond(postprocessMessage(result));
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
        REPLACES.put("b", Colors.BOLD);
        REPLACES.put("i", Colors.ITALICS);
        REPLACES.put("u", Colors.UNDERLINE);
        REPLACES.put("red", Colors.RED);
        REPLACES.put("brown", Colors.BROWN);
        REPLACES.put("green", Colors.GREEN);
    }

    @Override
    public void run() throws LogicException {
        init();
        sm.setDefault("maxLine", "256");
        sm.setDefault("ircPort", "6667");
        sm.setDefault("ircSsl", "0");
        final Builder builder = new Configuration.Builder().setLogin(sm.getStringSetting("login")).setName(sm.getStringSetting("nick"))
                .setNickservPassword(sm.getStringSetting("password"))
                .addServer(sm.getStringSetting("ircServer"), sm.getIntegerSetting("ircPort"))
                .addAutoJoinChannel("#" + sm.getStringSetting("join")).addListener(new Listener()).setAutoReconnect(true)
                .setAutoReconnectDelay(5000).setMaxLineLength(sm.getIntegerSetting("maxLine")).setMessageDelay(100);
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
            }
        }
    }

    @Override
    public void processMessage(ChatAdapter mucAdapted, BotMessage message) {
        throw new NotImplementedException();
    }

    @Override
    public String sendMessage(String message, String mucName) {
        bot.sendIRC().message(mucName, postprocessMessage(message));
        return null;
    }

    private String postprocessMessage(String message) {
        message = message.replace('\n', ' ');
        int i = 0;
        StringBuilder result = new StringBuilder();
        List<String> state = new LinkedList<>();
        boolean needRestore = false;
        while (i < message.length()) {
            final char ch = message.charAt(i);
            boolean codeFound = false;
            if (ch == '<') {
                for (Entry<String, String> r : REPLACES.entrySet()) {
                    if (message.length() > i + r.getKey().length() + 1
                            && message.substring(i + 1).toLowerCase().startsWith(r.getKey() + ">")) {
                        i += r.getKey().length() + 2;
                        result.append(r.getValue());
                        setState(state, r.getValue());
                        codeFound = true;
                    }
                    if (message.length() > i + r.getKey().length() + 2
                            && message.substring(i + 1).toLowerCase().startsWith("/" + r.getKey() + ">")) {
                        i += r.getKey().length() + 3;
                        removeState(state, r.getValue());
                        needRestore = true;
                        codeFound = true;
                    }
                }
            }
            if (!codeFound) {
                if (needRestore) {
                    restoreState(state, result);
                    needRestore = false;
                }
                result.append(ch);
                ++i;
            }
        }
        return result.toString();
    }

    private void removeState(List<String> state, String value) {
        if (Colors.BOLD.equals(value) || Colors.ITALICS.equals(value) || Colors.UNDERLINE.equals(value)) {
            state.remove(value);
        } else {
            state.remove("F_" + value);
        }
    }

    private void restoreState(List<String> state, StringBuilder result) {
        result.append(Colors.NORMAL);
        boolean colorSet = false;
        for (String s : state) {
            if (!s.startsWith("F_")) {
                result.append(s);
            } else if (!colorSet) {
                result.append(s.substring(2));
                colorSet = true;
            }
        }
    }

    private void setState(List<String> state, String value) {
        if (Colors.BOLD.equals(value) || Colors.ITALICS.equals(value) || Colors.UNDERLINE.equals(value)) {
            state.add(0, value);
        } else {
            state.add(0, "F_" + value);
        }
    }

    @Override
    public void sendMessage(String message) {
        bot.getUserBot().getChannels().forEach(c -> c.send().message(postprocessMessage(message)));
    }

    @Override
    public Set<String> getRoomsWithUser(String userId) {
        return Collections.emptySet();
    }

    @Override
    public ChatManager getChatManagerInstance() {
        return null;
    }

    @Override
    public MUCManager getMUCManager() {
        return null;
    }

    @Override
    public boolean isDirectChat(String roomId) {
        return !roomId.startsWith("#");
    }

}
