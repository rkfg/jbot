package me.rkfg.xmpp.bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.plugins.MessagePlugin;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.TCPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.iqversion.packet.Version;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

public class Main {

    private static final String PLUGINS_PACKAGE_NAME = "me.rkfg.xmpp.bot.plugins.";
    private static Logger log = LoggerFactory.getLogger(Main.class);
    private static String nick;
    private static List<ChatAdapter> mucsAdapted = new ArrayList<>();
    private static List<MultiUserChat> mucsList = new LinkedList<>();
    private static SettingsManager sm = SettingsManager.getInstance();
    private static ConcurrentLinkedQueue<BotMessage> outgoingMsgs = new ConcurrentLinkedQueue<BotMessage>();
    private static List<MessagePlugin> plugins = new LinkedList<MessagePlugin>();
    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static DiscussionHistory history = new DiscussionHistory();

    public static void main(String[] args) throws InterruptedException, SmackException, IOException {
        log.info("Starting up...");
        HibernateUtil.initSessionFactory("hibernate.cfg.xml");
        sm.setFilename("settings.ini");
        try {
            sm.loadSettings();
        } catch (FileNotFoundException e) {
            log.warn("settings.ini not found!", e);
            return;
        } catch (IOException e) {
            log.warn("settings.ini can't be read!", e);
            return;
        }
        HashMap<String, String> defaults = sm.getDefaults();
        defaults.put("nick", "Talho-san");
        defaults.put("login", "talho");
        defaults.put("resource", "jbot");

        nick = sm.getStringSetting("nick");
        String pluginClasses = sm.getStringSetting("plugins");
        loadPlugins(pluginClasses);
        log.info("Plugins loaded, initializing...");
        for (MessagePlugin plugin : plugins) {
            plugin.init();
        }
        log.info("Plugins initializion complete.");

        final XMPPConnection connection = new TCPConnection(sm.getStringSetting("server"));
        try {
            connection.connect();
            connection.login(sm.getStringSetting("login"), sm.getStringSetting("password"), sm.getStringSetting("resource"));
        } catch (XMPPException e) {
            log.warn("Connection error: ", e);
            return;
        }
        history.setMaxStanzas(0);
        final String[] mucs = org.apache.commons.lang3.StringUtils.split(sm.getStringSetting("join"), ',');
        joinMUCs(connection, mucs);
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void reconnectionSuccessful() {
                log.warn("Reconnected, rejoining mucs: {}", (Object[]) mucs);
                try {
                    joinMUCs(connection, mucs);
                } catch (NotConnectedException e) {
                    log.warn("Not connected while rejoining: ", e);
                }
            }
        });
        ChatManager.getInstanceFor(connection).addChatListener(new ChatManagerListener() {

            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new MessageListener() {

                    @Override
                    public void processMessage(Chat chat, Message message) {
                        Main.processMessage(new ChatAdapterImpl(chat), message);
                    }
                });
            }
        });

        connection.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                Version version = new Version("Gekko-go console", "14.7", "Nirvash OpenFirmware v7.1");
                version.setFrom(packet.getTo());
                version.setTo(packet.getFrom());
                version.setType(Type.RESULT);
                version.setPacketID(packet.getPacketID());
                try {
                    connection.sendPacket(version);
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }, new AndFilter(new IQTypeFilter(Type.GET), new PacketTypeFilter(Version.class)));
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (outgoingMsgs.size() > 0) {
                        BotMessage msg = outgoingMsgs.poll();
                        if (msg != null) {
                            try {
                                msg.getChat().sendMessage(msg.getMessage());
                                Thread.sleep(1000);
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (NotConnectedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }, "Messages sender").start();

        log.info("Sub req: {}", connection.getRoster().getSubscriptionMode());
        while (true) {
            Thread.sleep(1000);
        }
    }

    private static void joinMUCs(final XMPPConnection connection, String[] mucs) throws NotConnectedException {
        for (MultiUserChat multiUserChat : mucsList) {
            multiUserChat.leave();
        }
        mucsList.clear();
        mucsAdapted.clear();
        for (String conf : mucs) {
            MultiUserChat muc = new MultiUserChat(connection, conf);
            try {
                muc.join(nick, "", history, SmackConfiguration.getDefaultPacketReplyTimeout());
                mucsList.add(muc);
                log.info("Joined {}", muc.getRoom());
            } catch (XMPPException e) {
                log.warn("Joining error: ", e);
            } catch (NoResponseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final ChatAdapter mucAdapted = new MUCAdapterImpl(muc);
            mucsAdapted.add(mucAdapted);

            muc.addMessageListener(new PacketListener() {

                @Override
                public void processPacket(Packet packet) {
                    processMessage(mucAdapted, (Message) packet);
                }
            });
            muc.addParticipantStatusListener(new DefaultParticipantStatusListener() {

                @Override
                public void kicked(String participant, String actor, String reason) {
                    sendMessage(mucAdapted, String.format("Ха-ха, загнали под шконарь %s! %s", StringUtils.parseResource(participant),
                            !reason.isEmpty() ? "Мотивировали тем, что " + reason : "Без всякой мотивации."));
                }
            });
        }
    }

    private static void loadPlugins(String pluginClassesNamesStr) {
        String[] pluginClassesNames = pluginClassesNamesStr.split(",\\s?");
        log.debug("Plugins found: {}", (Object) pluginClassesNames);
        for (String pluginName : pluginClassesNames) {
            try {
                Class<? extends MessagePlugin> clazz = Class.forName(PLUGINS_PACKAGE_NAME + pluginName).asSubclass(MessagePlugin.class);
                plugins.add(clazz.newInstance());
            } catch (ClassNotFoundException e) {
                log.warn("Couldn't load plugin {}: {}", pluginName, e);
            } catch (InstantiationException e) {
                log.warn("Couldn't load plugin {}: {}", pluginName, e);
            } catch (IllegalAccessException e) {
                log.warn("Couldn't load plugin {}: {}", pluginName, e);
            }
        }
    }

    public static void processMessage(final ChatAdapter chat, final Message message) {
        commandExecutor.submit(new Runnable() {

            @Override
            public void run() {
                if (nick.equals(StringUtils.parseResource(message.getFrom()))) {
                    return;
                }
                if (message.getSubject() != null && !message.getSubject().isEmpty()) {
                    return;
                }
                String text = message.getBody();
                log.info("<{}>: {}", message.getFrom(), text);
                for (MessagePlugin plugin : plugins) {
                    Pattern pattern = plugin.getPattern();
                    if (pattern != null) {
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            String result = plugin.process(message, matcher);
                            if (result != null && !result.isEmpty()) {
                                sendMessage(chat, StringEscapeUtils.unescapeHtml4(result));
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public static void sendMessage(ChatAdapter chatAdapter, String message) {
        outgoingMsgs.offer(new BotMessage(chatAdapter, message));
    }

    public static String getNick() {
        return nick;
    }

    public static void sendMUCMessage(String message) {
        sendMUCMessage(message, (Integer) null);
    }

    public static void sendMUCMessage(String message, Integer... toConfs) {
        List<Integer> toConfsList = Arrays.asList(toConfs);
        for (Integer i = 0; i < mucsAdapted.size(); i++) {
            ChatAdapter adapter = mucsAdapted.get(i);
            if ((toConfs[0] == null || toConfsList.contains(i)) && adapter != null) {
                sendMessage(adapter, message);
            }
        }
    }

    public static SettingsManager getSettingsManager() {
        return sm;
    }

    public static List<MessagePlugin> getPlugins() {
        return plugins;
    }
}
