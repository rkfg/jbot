package me.rkfg.xmpp.bot.plugins;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import me.rkfg.xmpp.bot.domain.Countdown;
import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;

public class CountdownCommandPlugin extends CommandPlugin {

    private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    private static final long NOTIFY_INTERVAL = TimeUnit.MINUTES.toMillis(5);
    List<String> showcmd = Arrays.asList("show", "search", "s", "показать", "поиск", "п");
    List<String> addcmd = Arrays.asList("add", "a", "добавить", "д");
    long msecs[] = new long[] { TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1) };
    String unitNames[] = { "дн", "ч", "м", "с" };
    private Locale locale = Locale.forLanguageTag("ru-RU");

    @Override
    public void init() {
        super.init();
        new Timer("Countdown timer", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    HibernateUtil.exec(session -> {
                        Date from = new Date(System.currentTimeMillis() - NOTIFY_INTERVAL);
                        Date to = new Date(System.currentTimeMillis() + NOTIFY_INTERVAL);
                        @SuppressWarnings("unchecked")
                        List<Countdown> countdowns = session.createCriteria(Countdown.class).add(Restrictions.between("date", from, to))
                                .add(Restrictions.isNull("notified")).list();
                        for (Countdown countdown : countdowns) {
                            String dateStr = getFormatFull().format(countdown.getDate());
                            if (Boolean.TRUE.equals(countdown.getGroupchat())) {
                                sendMUCMessage(String.format("%s, наступает событие: %s [%s]", countdown.getCreator(), countdown.getName(),
                                        dateStr), countdown.getRoom());
                            } else {
                                /*
                                 * sendMessage(new ChatAdapterImpl(getChatManagerInstance().createChat(countdown.getCreator(), null)),
                                 * String.format("Наступает событие: %s [%s]", countdown.getName(), dateStr));
                                 */ }
                            countdown.setNotified(true);
                        }
                        return null;
                    });
                } catch (GwtUtilException e) {
                    log.warn("{}", e);
                }
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    private DateFormat getFormatFull() {
        return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
    }

    @Override
    public String processCommand(Message message, Matcher matcher) throws GwtUtilException {
        String[] params = matcher.group(COMMAND_GROUP).split(" ");
        if (params.length < 2) {
            return "недостаточно параметров.";
        }
        String cmd = params[0].toLowerCase();
        if (showcmd.contains(cmd)) {
            TimeZone tz = null;
            int from = 1;
            if (params.length > 2) {
                tz = TimeZone.getTimeZone(params[1]);
                from = 2;
            }
            if (tz == null || tz.getRawOffset() == 0 && !params[1].equals("GMT")) { // crudely detect fallback
                tz = TimeZone.getDefault();
                from = 1;
            }
            return getCountdown(message, toEnd(params, from), tz);
        }
        if (addcmd.contains(cmd)) {
            try {
                return addCountdown(message, Arrays.copyOfRange(params, 1, params.length));
            } catch (Exception e) {
                return "ошибка разбора команды.";
            }
        }
        return null;
    }

    private String addCountdown(Message message, String[] params) throws ParseException, GwtUtilException {
        if (params.length < 3) {
            throw new RuntimeException();
        }
        Date date = null;
        String name = null;
        DateFormat formatShort = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        DateFormat formatShortFull = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL, locale);
        DateFormat formatFull = getFormatFull();
        if (params[1].matches("^\\d+:\\d+$")) {
            params[1] = params[1] + ":00"; // add seconds
        }
        if (params.length > 3) {
            // try with timezone
            try {
                if (params[2].matches("^GMT[+-]\\d+$")) {
                    params[2] = params[2] + ":00"; // add seconds
                }
                date = formatShortFull.parse(fromBeginning(params, 3));
                name = toEnd(params, 3);
            } catch (ParseException e) {
                // failed
            }
        }
        if (date == null) {
            date = formatShort.parse(fromBeginning(params, 2));
            name = toEnd(params, 2);
        }
        saveCountdown(name, date, message);
        return String.format("установлен отсчёт для события \"%s\" на дату %s", name, formatFull.format(date));
    }

    private String fromBeginning(String[] arr, int to) {
        return StringUtils.join(Arrays.copyOfRange(arr, 0, to), ' ');
    }

    private String toEnd(String[] arr, int from) {
        return StringUtils.join(Arrays.copyOfRange(arr, from, arr.length), ' ');
    }

    private void saveCountdown(final String name, final Date date, final Message message) throws GwtUtilException {
        HibernateUtil.exec(session -> {
            Countdown countdown = new Countdown();
            countdown.setDate(date);
            countdown.setName(name);
            boolean fromGroupchat = message.isFromGroupchat();
            countdown.setCreator(message.getNick());
            if (fromGroupchat) {
                countdown.setRoom(message.getFromRoom());
            } else {
                countdown.setRoom(message.getFrom());
            }
            countdown.setGroupchat(fromGroupchat);
            session.save(countdown);
            return null;
        });
    }

    private String getCountdown(final Message message, final String name, final TimeZone tz) throws GwtUtilException {
        return HibernateUtil.exec(session -> {
            Criteria criteria = session.createCriteria(Countdown.class).add(Restrictions.like("name", "%" + name + "%"));
            if (message.isFromGroupchat()) {
                criteria.add(Restrictions.eq("groupchat", true));
            } else {
                criteria.add(Restrictions.eq("groupchat", false)).add(Restrictions.eq("creator", message.getFrom()));
            }
            @SuppressWarnings("unchecked")
            List<Countdown> countdowns = criteria.list();
            if (countdowns.isEmpty()) {
                return "ничего не найдено.";
            }
            StringBuilder sb = new StringBuilder("найдены отсчёты до:\n");
            long now = System.currentTimeMillis();
            DateFormat formatFull = getFormatFull();
            formatFull.setTimeZone(tz);
            for (Countdown countdown : countdowns) {
                Date date = countdown.getDate();
                sb.append(countdown.getName()).append(" — ").append(formatFull.format(date));
                long left = date.getTime() - now;
                String suffix = "\n";
                if (left < 0) {
                    sb.append(", произошло ");
                    left = -left;
                    suffix = "назад\n";
                } else {
                    sb.append(", осталось ");
                }
                for (int i = 0; i < msecs.length; i++) {
                    long msec = msecs[i];
                    long uLeft = left / msec;
                    left = left % msec;
                    if (uLeft > 0) {
                        sb.append(uLeft).append(' ').append(unitNames[i]).append(". ");
                    }
                }
                sb.append(suffix);
            }
            return sb.toString();
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("c", "с");
    }

    @Override
    public String getManual() {
        return "установить таймер обратного отсчёта до какого-либо события.\n"
                + "Формат: <add|a|добавить|д> <дата> <время> [таймзона] <описание события>\n" + "Пример: " + PREFIX
                + "c a 01.09.16 08:00 Вставай, в школу пора!\n\n"
                + "Поиск по установленным таймерам (отдельно для лички и групповых чатов).\n"
                + "Формат: <show|search|s|показать|поиск|п> [таймзона] <описание события>\n" + "Пример: " + PREFIX + "c s GMT+6 школ\n\n"
                + "Таймзоны могут быть в коротком формате (PST, CEST, EST и т.д.) и в виде GMT-смещения (GMT+8, GMT-3 и т.д.)";
    }

}
