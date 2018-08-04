package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import org.jivesoftware.smackx.muc.Occupant;

import com.google.common.collect.Sets;

import me.rkfg.xmpp.bot.IBot;
import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.IBot.Protocol;
import me.rkfg.xmpp.bot.domain.Karma;
import me.rkfg.xmpp.bot.domain.KarmaHistory;
import me.rkfg.xmpp.bot.message.BotMessage;
import me.rkfg.xmpp.bot.nxt.NXTAPI;
import me.rkfg.xmpp.bot.nxt.Transaction;
import me.rkfg.xmpp.bot.xmpp.XMPPBot;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

public class KarmaPlugin extends CommandPlugin {

    private NXTAPI nxtapi;
    private String karmaAddress;

    @Override
    public void init() {
        SettingsManager settingsManager = getSettingsManager();
        karmaAddress = settingsManager.getStringSetting("karmaAddress");
        nxtapi = new NXTAPI(settingsManager.getStringSetting("nxtAPIAddress"), karmaAddress, settingsManager.getStringSetting("karmaPass"));
        new Timer("karma timer", true).schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    HibernateUtil.exec(session -> {
                        KarmaHistory karmaHistory = (KarmaHistory) session
                                .createQuery("from KarmaHistory kh order by kh.blockTimestamp desc").setMaxResults(1).uniqueResult();
                        Long since = 0L;
                        if (karmaHistory != null) {
                            since = karmaHistory.getBlockTimestamp() + 1;
                        }
                        List<Transaction> transactions = nxtapi.getTransactionsSince(since, 1L);
                        for (Transaction transaction : transactions) {
                            log.info("Processing tx: {}", transaction);
                            String jid = nxtapi.getMessage(transaction.getTxid()).toLowerCase();
                            if (jid.length() > 1) {
                                boolean add = true;
                                if (jid.startsWith("- ")) {
                                    add = false;
                                    jid = jid.substring(2);
                                }
                                if (jid.startsWith("+ ")) {
                                    jid = jid.substring(2);
                                }
                                if (jid.contains("@") && jid.contains(".") && !jid.contains(" ")) {
                                    Karma karma = (Karma) session.createQuery("from Karma k where k.jid = :jid").setString("jid", jid)
                                            .uniqueResult();
                                    if (karma == null) {
                                        karma = (Karma) session.merge(new Karma(jid));
                                    }
                                    Long change = transaction.getAmount() * (add ? 1 : -1);
                                    session.merge(new KarmaHistory(new Date(), transaction.getTimestamp(), karma, change));
                                    karma.setKarma(karma.getKarma() + change);
                                }
                            }
                        }
                        return null;
                    });
                } catch (GwtUtilException e) {
                    log.warn("{}", e);
                }
            }
        }, 10000, 10000);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String processCommand(final BotMessage message, final Matcher matcher) throws GwtUtilException {
        return HibernateUtil.exec(session -> {
            List<Karma> karmas;
            String filterNick = matcher.group(COMMAND_GROUP);
            if (filterNick != null) {
                filterNick = filterNick.trim().toLowerCase();
            }
            karmas = session.createQuery("from Karma k order by k.karma").list();
            StringBuilder sb = new StringBuilder("Карма участников:\n");
            Map<String, Occupant> occupants = ((XMPPBot) Main.INSTANCE).getMUCManager().listMUCOccupantsByJID(message.getFromRoom());
            for (Karma karma : karmas) {
                String name = karma.getJid();
                Occupant jidOccupant = occupants.get(name);
                String nick = null;
                if (jidOccupant != null) {
                    nick = jidOccupant.getNick();
                    name += " (aka " + antiHighlight(nick) + ")";
                }
                if (filterNick == null || (nick != null && nick.toLowerCase().contains(filterNick))) {
                    sb.append(name + ": " + karma.getKarma()).append("\n");
                }
            }
            return sb.toString();
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("k", "r", "к", "л");
    }

    @Override
    public String getManual() {
        return "посмотреть карму участников.\n" + "Формат: [начало JID'а участника]\n"
                + "Если не указывать JID, выводятся все известные аккаунты с кармой.\n"
                + "Для изменения кармы отправьте X NXT (где X — целое) на адрес " + karmaAddress
                + ", к платежу приложите сообщение с JID, для которого хотите увеличить карму на X пунктов. "
                + "Для уменьшения кармы поставьте перед JID знак минуса через пробел (например: - admin@domain.com).\n" + "Пример: "
                + PREFIX + "k admin";
    }
    
    @Override
    public Set<Protocol> getCompatibleProtocols() {
        return Sets.newHashSet(IBot.Protocol.XMPP);
    }
}
