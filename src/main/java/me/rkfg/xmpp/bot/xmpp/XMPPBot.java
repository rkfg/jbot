package me.rkfg.xmpp.bot.xmpp;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManager.MatchMode;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.parsing.ParsingExceptionCallback;
import org.jivesoftware.smack.parsing.UnparsablePacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqversion.packet.Version;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.BotBase;
import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.BotMessage;
import me.rkfg.xmpp.bot.message.XMPPMessage;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;

public class XMPPBot extends BotBase {

    private Logger log = LoggerFactory.getLogger(Main.class);
    private MUCManager mucManager = new MUCManager();
    private ExecutorService outgoingMsgsExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    private XMPPTCPConnection connection;
    private String[] mucs;

    @Override
    public int run() {
        init();
        mucs = org.apache.commons.lang3.StringUtils.split(sm.getStringSetting("join"), ',');
        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder().setServiceName(sm.getStringSetting("server"))
                .build();
        connection = new XMPPTCPConnection(conf);
        connection.setParsingExceptionCallback(new ParsingExceptionCallback() {
            @Override
            public void handleUnparsablePacket(UnparsablePacket stanzaData) throws Exception {
                log.warn("Parsing error: {}\nContent:{}", stanzaData.getParsingException(), stanzaData.getContent());
            }
        });
        connection.addConnectionListener(new AbstractConnectionListener() {
            @Override
            public void reconnectionSuccessful() {
                log.warn("Reconnected, rejoining mucs: {}", org.apache.commons.lang3.StringUtils.join((Object[]) mucs, ", "));
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

            @Override
            public void reconnectionFailed(Exception e) {
                connect();
            }
        });
        connect();
        ChatManager.setDefaultMatchMode(MatchMode.SUPPLIED_JID);
        getChatManagerInstance().addChatListener((Chat chat, boolean createdLocally) -> chat
                .addMessageListener((chat2, message) -> processMessage(new ChatAdapterImpl(chat2), new XMPPMessage(message))));
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
                    log.warn("{}", e);
                }
            }
        }, new AndFilter(IQTypeFilter.GET, new StanzaTypeFilter(Version.class)));
        final PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(10);
        pingManager.registerPingFailedListener(() -> pingManager.setPingInterval(10));
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return 0;
    }

    private void connect() {
        try {
            connection.connect();
            connection.login(sm.getStringSetting("login"), sm.getStringSetting("password"), sm.getStringSetting("resource"));
            joinMUCs(connection, mucs);
        } catch (XMPPException | SmackException | IOException e) {
            log.warn("Connection error: ", e);
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

    public void processMessage(final ChatAdapter chat, final BotMessage message) {
        commandExecutor.submit(() -> {
            if (nick.equals(XmppStringUtils.parseResource(message.getFrom()))) {
                return;
            }
            Message originalMessage = message.getOriginalMessage();
            if (originalMessage.getSubject() != null && !originalMessage.getSubject().isEmpty()) {
                return;
            }
            if (originalMessage.getExtension("replace", "urn:xmpp:message-correct:0") != null) {
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
                        } catch (Exception e) {
                            log.warn("{}", e);
                        }
                    }
                }
            }
        });
    }

    public void sendMessage(final ChatAdapter chatAdapter, final String message) {
        outgoingMsgsExecutor.execute(() -> {
            try {
                chatAdapter.sendMessage(message);
                Thread.sleep(1000);
            } catch (XMPPBotException e) {
                log.warn("{}", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void sendMessage(String message) {
        for (MUCParams mucParams : mucManager.listMUCParams()) {
            sendMessage(mucParams.getMucAdapted(), message);
        }
    }

    @Override
    public String sendMessage(String message, String mucName) {
        for (MultiUserChat multiUserChat : mucManager.listMUCs()) {
            if (multiUserChat.getRoom().equals(mucName)) {
                sendMessage(mucManager.getMUCParams(multiUserChat).getMucAdapted(), message);
                return "";
            }
        }
        return null;
    }

    public MUCManager getMUCManager() {
        return mucManager;
    }

    @Override
    public Set<String> getRoomsWithUser(String userId) {
        return mucManager.listMUCs().stream().filter(muc -> muc.getOccupants().contains(userId)).map(MultiUserChat::getRoom)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isDirectChat(String roomId) {
        return mucManager.listMUCs().stream().noneMatch(m -> m.getRoom().equals(roomId));
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.XMPP;
    }
}
