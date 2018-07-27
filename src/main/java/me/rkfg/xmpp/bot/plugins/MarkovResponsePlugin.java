package me.rkfg.xmpp.bot.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;

import me.rkfg.xmpp.bot.domain.Markov;
import me.rkfg.xmpp.bot.domain.MarkovFirstWord;
import me.rkfg.xmpp.bot.domain.MarkovFirstWordCount;
import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;

public class MarkovResponsePlugin extends MessagePluginImpl {

    Random random = new Random();
    private static final int ANSWERS_LIMIT = 30;
    private LinkedBlockingDeque<Long> answersTimes = new LinkedBlockingDeque<>(ANSWERS_LIMIT + 1);
    private boolean cooldown = false;
    private String[] excuses = { "отстань, голова болит.", "устала я, потом поговорим.", "у меня ТЕ САМЫЕ часы, не видишь, что ли?",
            "Т___Т", "._.", ":[" };
    private static final int COOLDOWN_HOURS_MIN = 3;
    private static final int COOLDOWN_HOURS_MAX = 5;
    private static final int MIN_SEGMENTS = 2;
    private static final int SOFT_SEGMENT_LIMIT = 7;
    private static final int HARD_SEGMENT_LIMIT = 12;
    private static final int MIN_LAST_WORD_LENGTH = 5;
    private static final int ANSWERS_LIMIT_TIME = 5 * 60 * 1000; // 5 minutes in ms

    @Override
    public void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    if (answersTimes.size() > ANSWERS_LIMIT) {
                        answersTimes.poll();
                        Long first = answersTimes.peekFirst();
                        Long last = answersTimes.peekLast();
                        if (first != null && last != null && last - first < ANSWERS_LIMIT_TIME) {
                            cooldown = true;
                            Thread.sleep(random.nextInt(1000) + 1000);
                            sendMUCMessage("Устала вам отвечать. Отдохну.");
                            Thread.sleep(
                                    random.nextInt((COOLDOWN_HOURS_MAX - COOLDOWN_HOURS_MIN) * 3600000) + COOLDOWN_HOURS_MIN * 3600000);
                            cooldown = false;
                            sendMUCMessage("Отдохнула.");
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Cooldown thread").start();
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^" + getBotNick() + "[,: ] ?(.*)");
    }

    @Override
    public String process(final Message message, final Matcher matcher) {
        try {
            return message.getAppeal() + HibernateUtil.exec(session -> {
                if (cooldown) {
                    return excuse();
                }
                int hash = 0;
                int off = 0;
                char val[] = matcher.group(1).toCharArray();
                for (int i1 = 0; i1 < val.length; i1++) {
                    hash = 31 * hash + val[off++];
                }
                hash = (int) (hash * (System.currentTimeMillis() / 3600000));
                random.setSeed(hash);
                Object minmax[] = (Object[]) session.createQuery("select MIN(id), MAX(id) from Markov").uniqueResult();
                Long min = (Long) minmax[0];
                Long max = (Long) minmax[1];
                Markov segment = null;
                List<String> result = new LinkedList<>();
                String[] userWords = org.apache.commons.lang3.StringUtils.split(matcher.group(1));
                int userWordLength = 0;
                // find the longest word in the user input
                for (String word1 : userWords) {
                    userWordLength = Math.max(userWordLength, MarkovCollectorPlugin.purify(word1).length());
                }
                // user minLastWordLength if user's longest word is longer or that lognest word otherwise
                userWordLength = Math.min(userWordLength, MIN_LAST_WORD_LENGTH);
                for (int i2 = 0; i2 < 5; i2++) {
                    String word2 = MarkovCollectorPlugin.purify(userWords[random.nextInt(userWords.length)]);
                    if (!word2.isEmpty() && word2.length() >= userWordLength) {
                        segment = getRandomSegmentByFirstWord(word2, session);
                        if (segment != null && !segment.getText().isEmpty()) {
                            result.add(segment.getText());
                            break;
                        }
                    }
                }
                if (segment == null) {
                    segment = getFirstSegment(session, min, max, result);
                }
                int len = random.nextInt(SOFT_SEGMENT_LIMIT - MIN_SEGMENTS) + MIN_SEGMENTS;
                int leftToHard = HARD_SEGMENT_LIMIT - len;
                while (len-- > 0) {
                    segment = getRandomSegmentByFirstWord(segment.getLastWord(), session);
                    if (segment == null) {
                        break;
                    } else {
                        result.add(segment.getText());
                    }
                    if (len == 0 && segment.getLastWord().length() < MIN_LAST_WORD_LENGTH && leftToHard-- > 0) {
                        len = 1;
                    }
                }
                if (result.isEmpty()) {
                    getFirstSegment(session, min, max, result);
                }
                answersTimes.offer(System.currentTimeMillis());
                return org.apache.commons.lang3.StringUtils.join(result, " ");
            });
        } catch (GwtUtilException e) {
            log.warn("{}", e);
        }
        return "что-то пошло не так.";
    }

    protected String excuse() {
        return excuses[random.nextInt(excuses.length)];
    }

    private Markov getFirstSegment(Session session, Long min, Long max, List<String> result) {
        Markov segment;
        do {
            segment = (Markov) session.createQuery("from Markov m where m.id = :mid")
                    .setLong("mid", random.nextInt(max.intValue() - min.intValue()) + min).uniqueResult();
        } while (segment == null);
        result.add(segment.getFirstWord());
        result.add(segment.getText());
        return segment;
    }

    private Markov getRandomSegmentByFirstWord(String word, Session session) {
        MarkovFirstWordCount segmentsCount = (MarkovFirstWordCount) session.createQuery("from MarkovFirstWordCount where word = :fw")
                .setString("fw", word).uniqueResult();
        if (segmentsCount != null) {
            int n = random.nextInt(segmentsCount.getCount().intValue());
            try {
                MarkovFirstWord markovFirstWord = (MarkovFirstWord) session
                        .createQuery("from MarkovFirstWord where word = :fw and number = :n").setEntity("fw", segmentsCount)
                        .setInteger("n", n).uniqueResult();
                Markov segment = markovFirstWord.getMarkov();
                if (!segment.getText().isEmpty()) {
                    return segment;
                }
            } catch (NonUniqueResultException e) {
                log.warn("Non-unique word in the index found: {}/{}.", segmentsCount.getWord(), n);
            }
        }
        return null;
    }
}
