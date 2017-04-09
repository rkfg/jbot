package me.rkfg.xmpp.bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.chat.ChatManager.MatchMode;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqversion.packet.Version;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

public enum Bot {
    
    INSTANCE;
    
    private static final String PLUGINS_PACKAGE_NAME = "me.rkfg.xmpp.bot.plugins.";
    private Logger log = LoggerFactory.getLogger(Main.class);
    private String nick;
    private MUCManager mucManager = new MUCManager();
    private SettingsManager sm = SettingsManager.getInstance();
    private ExecutorService outgoingMsgsExecutor = Executors.newSingleThreadExecutor();
    private List<MessagePlugin> plugins = new LinkedList<MessagePlugin>();
    private ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    private XMPPTCPConnection connection;
    private String[] mucs;

    public void run() {
        log.info("Starting up...");
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
        sm.setDefault("nick", "Talho-san");
        sm.setDefault("login", "talho");
        sm.setDefault("resource", "jbot");
        sm.setDefault("usedb", "0");
        if (sm.getIntegerSetting("usedb") != 0) {
            HibernateUtil.initSessionFactory("hibernate.cfg.xml");
        }

        nick = sm.getStringSetting("nick");
        String pluginClasses = sm.getStringSetting("plugins");
        loadPlugins(pluginClasses);
        log.info("Plugins loaded, initializing...");
        for (MessagePlugin plugin : plugins) {
            plugin.init();
        }
        log.info("Plugins initializion complete.");
        mucs = org.apache.commons.lang3.StringUtils.split(sm.getStringSetting("join"), ',');
        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder().setServiceName(sm.getStringSetting("server"))
                .build();
        connection = new XMPPTCPConnection(conf);
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void reconnectionSuccessful() {
                log.warn("Reconnected, rejoining mucs.", org.apache.commons.lang3.StringUtils.join((Object[]) mucs, ", "));
                try {
                    joinMUCs(connection, mucs);
                } catch (NotConnectedException e) {
                    log.warn("Not connected while rejoining: ", e);
                }
            }

            @Override
            public void connectionClosed() {
                connect();
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                connect();
            }
        });
        connect();
        ChatManager.setDefaultMatchMode(MatchMode.SUPPLIED_JID);
        getChatManagerInstance().addChatListener(new ChatManagerListener() {

            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new ChatMessageListener() {

                    @Override
                    public void processMessage(Chat chat, Message message) {
                        Bot.this.processMessage(new ChatAdapterImpl(chat), message);
                    }
                });
            }
        });
        connection.addAsyncStanzaListener(new StanzaListener() {

            @Override
            public void processPacket(Stanza packet) throws NotConnectedException {
                Version version = new Version("Gekko-go console", "14.7", "Nirvash OpenFirmware v7.1");
                version.setFrom(packet.getTo());
                version.setTo(packet.getFrom());
                version.setType(Type.result);
                version.setStanzaId(packet.getStanzaId());
                try {
                    connection.sendStanza(version);
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }, new AndFilter(IQTypeFilter.GET, new StanzaTypeFilter(Version.class)));
        final PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(10);
        pingManager.registerPingFailedListener(new PingFailedListener() {

            @Override
            public void pingFailed() {
                pingManager.setPingInterval(10);
            }
        });
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void connect() {
        try {
            connection.connect();
            connection.login(sm.getStringSetting("login"), sm.getStringSetting("password"), sm.getStringSetting("resource"));
            joinMUCs(connection, mucs);
        } catch (XMPPException | SmackException | IOException e) {
            log.warn("Connection error: ", e);
            return;
        }
    }

    public ChatManager getChatManagerInstance() {
        return ChatManager.getInstanceFor(connection);
    }

    private void joinMUCs(final XMPPConnection connection, String[] mucs) throws NotConnectedException {
        mucManager.leave();
        for (String conf : mucs) {
            mucManager.join(connection, conf, nick);
        }
    }

    private void loadPlugins(String pluginClassesNamesStr) {
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

    public void processMessage(final ChatAdapter chat, final Message message) {
        commandExecutor.submit(new Runnable() {

            @Override
            public void run() {
                if (nick.equals(XmppStringUtils.parseResource(message.getFrom()))) {
                    return;
                }
                if (message.getSubject() != null && !message.getSubject().isEmpty()) {
                    return;
                }
                if (message.getExtension("replace", "urn:xmpp:message-correct:0") != null) {
                    // skip XEP-0308 corrections
                    return;
                }
                String text = message.getBody();
                log.info("<{}>: {}", message.getFrom(), text);
                for (MessagePlugin plugin : plugins) {
                    Pattern pattern = plugin.getPattern();
                    if (pattern != null) {
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            try {
                                String result = plugin.process(message, matcher);
                                if (result != null && !result.isEmpty()) {
                                    sendMessage(chat, StringEscapeUtils.unescapeHtml4(result));
                                    break;
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    public void sendMessage(final ChatAdapter chatAdapter, final String message) {
        outgoingMsgsExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    chatAdapter.sendMessage(message);
                    Thread.sleep(1000);
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getBotNick() {
        return nick;
    }

    public void sendMUCMessage(String message) {
        for (MUCParams mucParams : mucManager.listMUCParams()) {
            sendMessage(mucParams.getMucAdapted(), message);
        }
    }

    public void sendMUCMessage(String message, String mucName) {
        for (MultiUserChat multiUserChat : mucManager.listMUCs()) {
            if (multiUserChat.getRoom().equals(mucName)) {
                sendMessage(mucManager.getMUCParams(multiUserChat).getMucAdapted(), message);
                return;
            }
        }
    }

    public SettingsManager getSettingsManager() {
        return sm;
    }

    public List<MessagePlugin> getPlugins() {
        return plugins;
    }

    public MUCManager getMUCManager() {
        return mucManager;
    }
}
