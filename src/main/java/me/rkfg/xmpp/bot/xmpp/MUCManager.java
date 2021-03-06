package me.rkfg.xmpp.bot.xmpp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.XMPPMessage;

public class MUCManager {
    private static Logger log = LoggerFactory.getLogger(Main.class);
    private static DiscussionHistory history = new DiscussionHistory();
    private Map<MultiUserChat, MUCParams> mucsList = new ConcurrentHashMap<>();
    private Map<String, MultiUserChat> mucsJIDs = new ConcurrentHashMap<>();

    public MUCManager() {
        history.setMaxStanzas(0);
    }

    public Set<MultiUserChat> listMUCs() {
        return mucsList.keySet();
    }

    public MUCParams getMUCParams(MultiUserChat multiUserChat) {
        return mucsList.get(multiUserChat);
    }

    public Collection<MUCParams> listMUCParams() {
        return mucsList.values();
    }

    public Occupant getMUCOccupant(String fullMUCJID) {
        String room = XmppStringUtils.parseBareJid(fullMUCJID);
        return mucsJIDs.get(room).getOccupant(fullMUCJID);
    }

    // crutchy way to extract the occupants map, no direct access available
    /**
     * 
     * @param roomJID
     *            MUC JID to get occupants of.
     * @return map of JID (if JIDs are visible) or nick (otherwise) to occupant object.
     */
    public Map<String, Occupant> listMUCOccupantsByJID(String roomJID) {
        Map<String, Occupant> occupants = new HashMap<>();
        MultiUserChat multiUserChat = mucsJIDs.get(roomJID);
        for (String occupantName : multiUserChat.getOccupants()) {
            Occupant occupant = multiUserChat.getOccupant(occupantName);
            if (occupant.getJid() != null) {
                occupants.put(XmppStringUtils.parseBareJid(occupant.getJid()), occupant);
            } else {
                occupants.put(occupant.getNick(), occupant);
            }
        }
        return occupants;
    }

    public void leave(String mucName) {
        mucsList.keySet().stream().filter(k -> k.getRoom().equals(mucName)).forEach(t -> {
            try {
                t.leave();
            } catch (NotConnectedException e) {
                log.warn("{}", e);
            }
        });
    }

    public void leave() throws NotConnectedException {
        for (MultiUserChat multiUserChat : listMUCs()) {
            log.info("Leaving previously joined {}", multiUserChat.getRoom());
            multiUserChat.leave();
            log.info("Removing registered packet handlers...");
            MUCParams mucParams = getMUCParams(multiUserChat);
            multiUserChat.removeMessageListener(mucParams.getMessageListener());
            multiUserChat.removeParticipantStatusListener(mucParams.getParticipantStatusListener());
        }
        mucsList.clear();
        mucsJIDs.clear();
    }

    public void join(XMPPConnection connection, String conf, String nick) throws NotConnectedException {
        MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection)
                .getMultiUserChat(org.apache.commons.lang3.StringUtils.trim(conf));
        try {
            log.info("Joining {}", muc.getRoom());
            muc.join(nick, "", history, SmackConfiguration.getDefaultPacketReplyTimeout());
        } catch (XMPPException e) {
            log.warn("Joining error: ", e);
        } catch (NoResponseException e) {
            /**
             * Некоторые хуесосы-владельцы серверов не соблюдают XEP-0045 и респонс не посылают. Реально бот заходит в конфу, но эксепшон
             * всё равно валится. Чтобы с ними бороться, надо либо секурити отключать, либо поддельный респонс делать где-то в кишках этого
             * джойна. А можно просто игнорировать это. (Энивей mucsAdapted разрастается, хотя не должен в этом случае, поэтому быдлокод
             * сильно хуже не стал)
             **/
            log.warn("{}", e);
        }

        MUCParams mucParams = new MUCParams();

        final ChatAdapter mucAdapted = new MUCAdapterImpl(muc);
        mucParams.setMucAdapted(mucAdapted);

        MessageListener messageListener = message -> ((XMPPBot) Main.INSTANCE).processMessage(mucAdapted, new XMPPMessage(message));
        muc.addMessageListener(messageListener);
        mucParams.setMessageListener(messageListener);
        mucsList.put(muc, mucParams);
        mucsJIDs.put(muc.getRoom(), muc);
        log.info("Joined {}", muc.getRoom());
    }

}
