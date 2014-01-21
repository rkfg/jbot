package me.rkfg.xmpp.bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.plugins.GoogleCommandPlugin;
import me.rkfg.xmpp.bot.plugins.MarkovCollectorPlugin;
import me.rkfg.xmpp.bot.plugins.MarkovResponsePlugin;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import me.rkfg.xmpp.bot.plugins.MessagePluginImpl;
import me.rkfg.xmpp.bot.plugins.TitlePlugin;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private static String nick;
    private static ChatAdapter mucAdapted;
    private static SettingsManager sm = SettingsManager.getInstance();
    private static ConcurrentLinkedQueue<BotMessage> outgoingMsgs = new ConcurrentLinkedQueue<BotMessage>();
    public static List<MessagePluginImpl> plugins = new LinkedList<MessagePluginImpl>(Arrays.asList(new GoogleCommandPlugin(),
            new MarkovResponsePlugin(), new TitlePlugin(), new MarkovCollectorPlugin()));
    private static ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws InterruptedException {
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
        for (MessagePlugin plugin : plugins) {
            plugin.init();
        }

        final Connection connection = new XMPPConnection(sm.getStringSetting("server"));
        try {
            connection.connect();
            connection.login(sm.getStringSetting("login"), sm.getStringSetting("password"), sm.getStringSetting("resource"));
        } catch (XMPPException e) {
            log.warn("Connection error: ", e);
            return;
        }
        final MultiUserChat muc = new MultiUserChat(connection, sm.getStringSetting("join"));
        final DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void reconnectionSuccessful() {
                try {
                    muc.join(nick, "", history, SmackConfiguration.getPacketReplyTimeout());
                } catch (XMPPException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        try {
            muc.join(nick, "", history, SmackConfiguration.getPacketReplyTimeout());
        } catch (XMPPException e) {
            log.warn("Joining error: ", e);
        }
        mucAdapted = new MUCAdapterImpl(muc);

        muc.addMessageListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                processMessage(mucAdapted, (Message) packet);
            }
        });
        muc.addParticipantStatusListener(new DefaultParticipantStatusListener() {
            @Override
            public void joined(String participant) {
                // try {
                // muc.sendMessage(String.format("%s, пошёл нахуй\nтупица.", StringUtils.parseResource(participant)));
                // } catch (XMPPException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
            }

            @Override
            public void kicked(String participant, String actor, String reason) {
                sendMessage(mucAdapted, String.format("Ха-ха, загнали под шконарь %s! %s", StringUtils.parseResource(participant),
                        !reason.isEmpty() ? "Мотивировали тем, что " + reason : "Без всякой мотивации."));
            }
        });

        connection.getChatManager().addChatListener(new ChatManagerListener() {

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
                Version version = new Version();
                version.setOs("Nirvash OpenFirmware v7.1");
                version.setName("Gekko-go console");
                version.setVersion("14.7");
                version.setFrom(packet.getTo());
                version.setTo(packet.getFrom());
                version.setType(Type.RESULT);
                version.setPacketID(packet.getPacketID());
                connection.sendPacket(version);
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

        while (true) {
            Thread.sleep(1000);
        }
    }

    public static void processMessage(final ChatAdapter chat, final Message message) {
        commandExecutor.submit(new Runnable() {

            @Override
            public void run() {
                if (nick.equals(StringUtils.parseResource(message.getFrom()))) {
                    return;
                }
                String text = message.getBody();
                log.info("<{}>: {}", message.getFrom(), text);
                for (MessagePlugin plugin : plugins) {
                    Matcher matcher = plugin.getPattern().matcher(text);
                    if (matcher.find()) {
                        String result = plugin.process(message, matcher);
                        if (result != null && !result.isEmpty()) {
                            sendMessage(chat, StringEscapeUtils.unescapeHtml4(result));
                            break;
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
        if (mucAdapted != null) {
            sendMessage(mucAdapted, message);
        }
    }
}
