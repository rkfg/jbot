/**
 * Plugin for Faggot of the Day game in MUCs.
 * Copyright (C) 2017 Kona-chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.rkfg.xmpp.bot.plugins;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.NonUniqueResultException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.MUCManager;
import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.domain.Contender;
import me.rkfg.xmpp.bot.domain.Winning;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

/**
 * Lets users of MUCs play Faggot of the Day.
 *
 * @author Kona-chan
 * @version 0.1.0
 */
public final class FaggotOfTheDayPlugin extends CommandPlugin {

    private static final List<String> ALL_COMMANDS = Arrays.asList("pidor", "пидор");

    private static final String INFO_NO_WINNER_TODAY =
            "сегодня Пидора дня нет.";
    private static final String INFO_TODAYS_WINNER_IS =
            "сегодня Пидор дня — ";
    private static final String INFO_A_WINNER_IS_YOU = INFO_TODAYS_WINNER_IS + "ты!";

    private final MUCManager mucManager = Main.getMUCManager();
    private final SettingsManager settingsManager = Main.getSettingsManager();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Random random = new Random();

    private boolean isListening = false;

    @Override
    public String processCommand(Message message, Matcher matcher)
            throws LogicException, ClientAuthException {
        startListeningIfNeeded();

        return roll(message);
    }

    private void startListeningIfNeeded() {
        if (isListening) {
            return;
        }
        isListening = true;

        mucManager.listMUCs().forEach(muc -> {
            prefillContendersIfNeeded(muc);

            muc.addParticipantListener(presence -> {
                if (presence.getType().equals(Presence.Type.available)) {
                    updateContenderOnJoin(muc, presence);
                }
            });
        });
    }

    /**
     * Checks if at least one Contender from a given MUC has been registered
     * and adds all users from that MUC as Contenders if not.
     *
     * @param muc MUC that needs to be checked.
     */
    private void prefillContendersIfNeeded(MultiUserChat muc) {
        // TODO: move exception handling to the calling method
        try {
            HibernateUtil.exec(session -> {
                if (session
                        .createQuery("from Contender cont where cont.room = :room")
                        .setParameter("room", muc.getRoom())
                        .list()
                        .isEmpty()) {
                    muc
                    .getOccupants()
                    .stream()
                    .map(muc::getOccupant)
                    .map(occupant -> {
                        // TODO: refactor to a separate method
                        final String jid = Optional
                                .ofNullable(occupant.getJid())
                                .map(XmppStringUtils::parseBareJid)
                                .orElse(null);
                        if (jid == null || jid.equals(getBotJid())) {
                            return null;
                        }
                        final String nick = occupant.getNick();
                        final String room = muc.getRoom();
                        return new Contender(nick, jid, room);
                    })
                    .filter(c -> c != null) // TODO: check if this is needed
                    .forEach(session::merge);
                }
                return null;
            });
        } catch (LogicException | ClientAuthException e) {
            logger.warn("HibernateUtil.exec exception: ", e);
        }
    }

    /**
     * Monitors a MUC for new occupants and adds them to the database.
     *
     * @param muc MUC that needs to be monitored.
     * @param presence Presence object of occupant.
     */
    private void updateContenderOnJoin(MultiUserChat muc, Presence presence) {
        // TODO: move exception handling to the calling method
        try {
            HibernateUtil.exec(session -> {
                // TODO: refactor to a separate method
                final Occupant occupant = muc.getOccupant(presence.getFrom());
                final String jid = Optional
                        .ofNullable(occupant.getJid())
                        .map(XmppStringUtils::parseBareJid)
                        .orElse(null);
                if (jid == null || jid.equals(getBotJid())) {
                    return null;
                }
                final String nick = occupant.getNick();
                final String room = muc.getRoom();

                @SuppressWarnings("unchecked")
                final Contender contender = (Contender) session
                        .createQuery("from Contender where jid = :jid and room = :room")
                        .setString("jid", jid)
                        .setString("room", room)
                        .uniqueResult();

                if (contender == null) {
                    session.merge(new Contender(nick, jid, room));
                } else {
                    contender.setNick(nick);
                }

                return null;
            });
        } catch (LogicException | ClientAuthException | NonUniqueResultException e) {
            logger.warn("HibernateUtil.exec exception: ", e);
        }
    }

    private String roll(Message message) {
        final Contender winner = getTodaysWinner();
        if (winner == null || winner.getJid() == null) {
            return INFO_NO_WINNER_TODAY;
        }

        final String senderJid = XmppStringUtils.parseBareJid(mucManager
                .getMUCOccupant(message.getFrom())
                .getJid());
        if (senderJid != null && senderJid.equals(winner.getJid())) {
            return INFO_A_WINNER_IS_YOU;
        }

        // Search MUC for winner's JID and fetch their current nickname if they are
        // available, use fallback nickname otherwise.
        final String roomJID = XmppStringUtils.parseBareJid(message.getFrom());
        final String nick = mucManager
                .listMUCs()
                .stream()
                .filter(muc -> muc.getRoom().equals(roomJID))
                .flatMap(muc -> muc.getOccupants().stream().map(muc::getOccupant))
                .filter(o -> o.getJid() != null && o.getJid().equals(winner.getJid()))
                .findFirst()
                .map(o -> o.getNick())
                .orElse(winner.getNick());
        return INFO_TODAYS_WINNER_IS + nick + ".";
    }

    @SuppressWarnings("unchecked")
    private Contender getTodaysWinner() {
        try {
            return HibernateUtil.exec(session -> {
                // One winner per day. If there is no winner for a given date,
                // generate one and return them.
                final List<Winning> winnings = session
                        .createQuery("from Winning win where win.date = :current_date")
                        .setDate("current_date", getToday())
                        .list();

                Contender winner;
                if (winnings.isEmpty()) {
                    final List<Contender> contenders = session
                            .createQuery("from Contender")
                            .list();
                    if (contenders.isEmpty()) {
                        return null;
                    }

                    winner = contenders.get(random.nextInt(contenders.size()));
                    session.merge(new Winning(getToday(), winner));
                } else {
                    winner = winnings.get(0).getContender();
                }

                return winner;
            });
        } catch (LogicException | ClientAuthException e) {
            logger.warn("HibernateUtil.exec exception: ", e);
            return null;
        }
    }

    private Date getToday() {
        final LocalTime midnight = LocalTime.MIDNIGHT;
        final LocalDate today = LocalDate.now();
        final LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
        return Date.from(todayMidnight.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String getBotJid() {
        final String login = settingsManager.getStringSetting("login");
        final String server = settingsManager.getStringSetting("server");
        return login + "@" + server;
    }

    @Override
    public List<String> getCommand() {
        return ALL_COMMANDS;
    }

    @Override
    public String getManual() {
        final String commands = StringUtils.join(ALL_COMMANDS, "|");
        final String sampleCommand = PREFIX + ALL_COMMANDS.get(0);

        return "узнать, кто сегодня Пидор дня.\n" +
        "Формат: <" + commands + ">\n" +
        "Пример: " + sampleCommand;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void listContendersAndWinnings() {
        try {
            HibernateUtil.exec(session -> {
                logger.info("Contenders:");
                session.createQuery("from Contender").list().forEach(
                        contender -> logger.info("{}", contender));

                logger.info("Winnings:");
                session.createQuery("from Winning").list().forEach(
                        winning -> logger.info("{}", winning));

                return null;
            });
        } catch (LogicException | ClientAuthException e) {
            logger.warn("HibernateUtil.exec exception: ", e);
        }
    }

}
