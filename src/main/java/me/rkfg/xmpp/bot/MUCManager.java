package me.rkfg.xmpp.bot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MUCManager {
    private static Logger log = LoggerFactory.getLogger(Main.class);
    private static DiscussionHistory history = new DiscussionHistory();
    private Map<MultiUserChat, MUCParams> mucsList = new HashMap<MultiUserChat, MUCParams>();

    public MUCManager() {
        history.setMaxStanzas(0);
    }

    public Set<MultiUserChat> listMUCs() {
        return mucsList.keySet();
    }

    public MUCParams getMUCParams(MultiUserChat multiUserChat) {
        return mucsList.get(multiUserChat);
    }

    public void put(MultiUserChat muc, MUCParams mucParams) {
        mucsList.put(muc, mucParams);
    }

    public Collection<MUCParams> listMUCParams() {
        return mucsList.values();
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
    }

    public void join(XMPPConnection connection, String conf, String nick) throws NotConnectedException {
        MultiUserChat muc = new MultiUserChat(connection, org.apache.commons.lang3.StringUtils.trim(conf));
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
            e.printStackTrace();
        }

        MUCParams mucParams = new MUCParams();

        final ChatAdapter mucAdapted = new MUCAdapterImpl(muc);
        mucParams.setMucAdapted(mucAdapted);

        PacketListener messageListener = new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                Main.processMessage(mucAdapted, (Message) packet);
            }
        };
        muc.addMessageListener(messageListener);
        mucParams.setMessageListener(messageListener);
        DefaultParticipantStatusListener participantStatusListener = new DefaultParticipantStatusListener() {

            @Override
            public void kicked(String participant, String actor, String reason) {
                Main.sendMessage(mucAdapted, String.format("Ха-ха, загнали под шконарь %s! %s", StringUtils.parseResource(participant),
                        !reason.isEmpty() ? "Мотивировали тем, что " + reason : "Без всякой мотивации."));
            }
        };
        muc.addParticipantStatusListener(participantStatusListener);
        mucParams.setParticipantStatusListener(participantStatusListener);
        put(muc, mucParams);
        log.info("Joined {}", muc.getRoom());

    }

}
