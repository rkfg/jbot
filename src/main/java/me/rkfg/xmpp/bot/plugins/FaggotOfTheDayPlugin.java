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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jxmpp.util.XmppStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.MUCManager;
import me.rkfg.xmpp.bot.domain.Contender;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.HibernateUtil.ListQueryFilter;
import ru.ppsrk.gwt.server.SettingsManager;

/**
 * Lets users of MUCs play Faggot of the Day.
 *
 * @author Kona-chan
 * @version 0.2.0
 */
public final class FaggotOfTheDayPlugin extends CommandPlugin {

    private static final List<String> ALL_COMMANDS = Arrays.asList("pidor", "пидор");

    private static final String INFO_NO_WINNER_TODAY =
            "сегодня Пидора дня нет.";
    private static final String INFO_TODAYS_WINNER_IS =
            "сегодня Пидор дня — ";
    private static final String INFO_A_WINNER_IS_YOU = INFO_TODAYS_WINNER_IS + "ты!";

    private final MUCManager mucManager = getMUCManager();
    private final SettingsManager settingsManager = getSettingsManager();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Random random = new Random();

    private Timer timer;
    private Contender winner;

    @Override
    public String processCommand(Message message, Matcher matcher)
            throws LogicException, ClientAuthException {
        startListeningIfNeeded();

        if (winner == null || winner.getJid() == null) {
            return INFO_NO_WINNER_TODAY;
        }

        final String senderJid = bareJid(mucManager
                .getMUCOccupant(message.getFrom())
                .getJid());
        if (senderJid != null && senderJid.equals(winner.getJid())) {
            return INFO_A_WINNER_IS_YOU;
        }

        // Search MUC for winner's JID and fetch their current nickname if they are
        // available, use fallback nickname otherwise.
        final String roomJID = bareJid(message.getFrom());
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

    private void startListeningIfNeeded() {
        if (timer != null) {
            return;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    logger.info("");
                    logger.info("A new day starts in the Empire.");
                    logger.info("");

                    final List<Contender> contenders = HibernateUtil.queryList(
                            "from Contender cont where cont.loggedInYesterday is true",
                            null, null, (ListQueryFilter) null);
                    logger.info("Today's contenders:");
                    contenders.forEach(c -> logger.info("{}", c.toString()));
                    logger.info("");

                    contenders.forEach(c -> c.setLoggedInYesterday(false));

                    if (contenders.isEmpty()) {
                        logger.info("There are no contenders today.");
                        logger.info("");
                    } else {
                        winner = contenders.get(random.nextInt(contenders.size()));
                        logger.info("Today's winner is {}! Congrats!", winner.toString());
                        HibernateUtil.saveObject(winner);
                    }

                    mucManager.listMUCs().forEach(muc -> addContendersFromMuc(muc));

                } catch (LogicException | ClientAuthException e) {
                    handleHibernateUtilException(e);
                }
            }

        }, getToday(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

        mucManager.listMUCs().forEach(muc -> {
            muc.addParticipantListener(presence -> {
                if (presence.getType().equals(Presence.Type.available)) {
                    updateContenderOnJoin(muc, presence);
                }
            });
        });
    }

    /**
     * Adds contenders from MUC occupants and sets loggedInYesterday of existing ones to true.
     *
     * @param muc
     */
    private void addContendersFromMuc(MultiUserChat muc) {
        try {
            final List<Contender> contenders = HibernateUtil.queryList(
                    "from Contender cont where cont.room = :room",
                    new String[] { "room" },
                    new Object[] { muc.getRoom() },
                    (ListQueryFilter) null);

            muc.getOccupants()
                .stream()
                .map(muc::getOccupant)
                .filter(o -> o.getJid() != null && !bareJid(o.getJid()).equals(getBotJid()))
                .forEach(o -> {
                    final Optional<Contender> contender = contenders
                            .stream()
                            .filter(c -> c.getJid().equals(bareJid(o.getJid())))
                            .findFirst();
                    try {
                        if (contender.isPresent()) {
                            logger.debug("Updating existing contender {}", contender.get().toString());
                            contender.get().setLoggedInYesterday(true);
                            HibernateUtil.saveObject(contender.get());
                        } else {
                            logger.debug("Creating new contender with jid {}", o.getJid());
                            HibernateUtil.saveObject(new Contender(
                                    o.getNick(), bareJid(o.getJid()), muc.getRoom(), true));
                        }
                    } catch (LogicException | ClientAuthException e) {
                        handleHibernateUtilException(e);
                    }
                });

        } catch (LogicException | ClientAuthException e) {
            handleHibernateUtilException(e);
        }
    }

    /**
     * Monitors a MUC for new occupants and adds them to the database.
     *
     * @param muc MUC that needs to be monitored.
     * @param presence Presence object of occupant.
     */
    private void updateContenderOnJoin(MultiUserChat muc, Presence presence) {
        final Occupant occupant = muc.getOccupant(presence.getFrom());
        if (occupant.getJid() == null || bareJid(occupant.getJid()).equals(getBotJid())) {
            return;
        }

        try {
            final List<Contender> contenders = HibernateUtil.queryList(
                    "from Contender where jid = :jid and room = :room",
                    new String[] { "jid", "room" },
                    new Object[] { bareJid(occupant.getJid()), occupant.getNick() },
                    (ListQueryFilter) null);
            if (contenders.isEmpty()) {
               HibernateUtil.saveObject(new Contender(
                       occupant.getNick(), bareJid(occupant.getJid()), muc.getRoom(), true));
            } else {
                final Contender contender = contenders.get(0);
                contender.setNick(occupant.getNick());
                contender.setLoggedInYesterday(true);
                HibernateUtil.saveObject(contender);
            }

        } catch (LogicException | ClientAuthException e) {
            handleHibernateUtilException(e);
        }
    }

    private static String bareJid(String jid) {
        return XmppStringUtils.parseBareJid(jid);
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

    private void handleHibernateUtilException(Exception e) {
        // If I ignore it, maybe it will go away.
        logger.warn("HibernateUtil exception: ", e);
    }

}
