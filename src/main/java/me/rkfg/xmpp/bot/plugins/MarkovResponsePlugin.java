package me.rkfg.xmpp.bot.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.domain.Markov;
import me.rkfg.xmpp.bot.domain.MarkovFirstWord;
import me.rkfg.xmpp.bot.domain.MarkovFirstWordCount;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;

public class MarkovResponsePlugin extends MessagePluginImpl {

    Random random = new Random();
    private static final int answersLimit = 30;
    private LinkedBlockingDeque<Long> answersTimes = new LinkedBlockingDeque<Long>(answersLimit + 1);
    private boolean cooldown = false;
    private String[] excuses = { "отстань, голова болит.", "устала я, потом поговорим.", "у меня ТЕ САМЫЕ часы, не видишь, что ли?",
            "Т___Т", "._.", ":[" };
    private final static int cooldownHoursMin = 3;
    private final static int cooldownHoursMax = 5;
    private final static int minSegments = 2;
    private final static int softSegmentLimit = 7;
    private final static int hardSegmentLimit = 12;
    private final static int minLastWordLength = 5;
    private static final int answersLimitTime = 5 * 60 * 1000; // 5 minutes in ms

    @Override
    public void init() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        if (answersTimes.size() > answersLimit) {
                            answersTimes.poll();
                            Long first = answersTimes.peekFirst();
                            Long last = answersTimes.peekLast();
                            if (first != null && last != null) {
                                if (last - first < answersLimitTime) {
                                    cooldown = true;
                                    Thread.sleep(random.nextInt(1000) + 1000);
                                    Main.sendMUCMessage("Устала вам отвечать. Отдохну.");
                                    Thread.sleep(random.nextInt((cooldownHoursMax - cooldownHoursMin) * 3600000) + cooldownHoursMin
                                            * 3600000);
                                    cooldown = false;
                                    Main.sendMUCMessage("Отдохнула.");
                                }
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }, "Cooldown thread").start();
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^" + Main.getNick() + "[,: ] ?(.*)");
    }

    @Override
    public String process(final Message message, final Matcher matcher) {
        try {
            return StringUtils.parseResource(message.getFrom()) + ", " + HibernateUtil.exec(new HibernateCallback<String>() {

                @Override
                public String run(Session session) throws LogicException, ClientAuthException {
                    if (cooldown) {
                        return excuse();
                    }
                    int hash = 0;
                    int off = 0;
                    char val[] = matcher.group(1).toCharArray();
                    for (int i = 0; i < val.length; i++) {
                        hash = 31 * hash + val[off++];
                    }
                    hash = (int) (hash * (System.currentTimeMillis() / 3600000));
                    random.setSeed(hash);
                    Object minmax[] = (Object[]) session.createQuery("select MIN(id), MAX(id) from Markov").uniqueResult();
                    Long min = (Long) minmax[0];
                    Long max = (Long) minmax[1];
                    Markov segment = null;
                    List<String> result = new LinkedList<String>();
                    String[] userWords = org.apache.commons.lang3.StringUtils.split(matcher.group(1));
                    for (int i = 0; i < 5; i++) {
                        String word = MarkovCollectorPlugin.purify(userWords[random.nextInt(userWords.length)]);
                        if (!word.isEmpty() && word.length() >= minLastWordLength) {
                            segment = getRandomSegmentByFirstWord(word, session);
                            if (segment != null && !segment.getText().isEmpty()) {
                                result.add(segment.getText());
                                break;
                            }
                        }
                    }
                    if (segment == null) {
                        segment = getFirstSegment(session, min, max, result);
                    }
                    int len = random.nextInt(softSegmentLimit - minSegments) + minSegments;
                    int leftToHard = hardSegmentLimit - len;
                    while (len-- > 0) {
                        segment = getRandomSegmentByFirstWord(segment.getLastWord(), session);
                        if (segment == null) {
                            break;
                        } else {
                            result.add(segment.getText());
                        }
                        if (len == 0 && segment.getLastWord().length() < minLastWordLength && leftToHard-- > 0) {
                            len = 1;
                        }
                    }
                    if (result.isEmpty()) {
                        getFirstSegment(session, min, max, result);
                    }
                    answersTimes.offer(System.currentTimeMillis());
                    return org.apache.commons.lang3.StringUtils.join(result, " ");
                }
            });
        } catch (ClientAuthException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LogicException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
