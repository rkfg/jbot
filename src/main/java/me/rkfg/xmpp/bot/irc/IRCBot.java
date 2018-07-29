package me.rkfg.xmpp.bot.irc;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.chat.ChatManager;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
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

    private class Listener extends ListenerAdapter {
        @Override
        public void onMessage(MessageEvent event) throws Exception {
            processMessage(new IRCMessage(event));
        }

        @Override
        public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
            processMessage(new IRCMessage(event));
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
                                ircMessage.getOriginalMessage().respond(result.replace('\n', ' '));
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
    public void run() throws LogicException {
        init();
        sm.setDefault("maxLine", "256");
        Configuration conf = new Configuration.Builder().setLogin(sm.getStringSetting("login")).setName(sm.getStringSetting("nick"))
                .setNickservPassword(sm.getStringSetting("password")).addServer(sm.getStringSetting("ircServer"))
                .addAutoJoinChannel("#" + sm.getStringSetting("join")).addListener(new Listener()).setAutoReconnect(true)
                .setAutoReconnectDelay(5000).setMaxLineLength(sm.getIntegerSetting("maxLine")).buildConfiguration();
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
        bot.sendIRC().message(mucName, removeLineBreaks(message));
        return null;
    }

    private String removeLineBreaks(String message) {
        return message.replace('\n', ' ');
    }

    @Override
    public void sendMessage(String message) {
        bot.getUserBot().getChannels().forEach(c -> c.send().message(removeLineBreaks(message)));
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
