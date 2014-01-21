package me.rkfg.xmpp.bot.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.shared.SharedUtils;

public class MarkovResponsePlugin extends MessagePluginImpl {

    Random random = new Random();
    private int answersLimit = 0;
    private String[] excuses = { "отстань, голова болит.", "устала я, потом поговорим.", "у меня ТЕ САМЫЕ часы, не видишь, что ли?",
            "Т___Т", "._.", ":[" };
    private final static int cooldownHoursMin = 3;
    private final static int cooldownHoursMax = 5;
    private final static int minSegments = 2;
    private final static int softSegmentLimit = 7;
    private final static int hardSegmentLimit = 12;
    private final static int minLastWordLength = 5;

    @Override
    public void init() {
        answersLimit = random.nextInt(50) + 50;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        if (answersLimit == 0) {
                            Thread.sleep(random.nextInt(1000) + 1000);
                            Main.sendMUCMessage("Устала вам отвечать. Отдохну.");
                            Thread.sleep(random.nextInt((cooldownHoursMax - cooldownHoursMin) * 3600000) + cooldownHoursMin * 3600000);
                            answersLimit = random.nextInt(50) + 50;
                            Main.sendMUCMessage("Отдохнула.");
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
                public String run(Session session) throws LogicException, ClientAuthenticationException {
                    if (answersLimit == 0) {
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
                    do {
                        segment = (Markov) session.createQuery("from Markov m where m.id = :mid")
                                .setLong("mid", random.nextInt(max.intValue() - min.intValue()) + min).uniqueResult();
                    } while (segment == null);
                    List<String> result = new LinkedList<String>();
                    result.add(segment.getFirstWord());
                    result.add(segment.getText());
                    int len = random.nextInt(softSegmentLimit - minSegments) + minSegments;
                    int leftToHard = hardSegmentLimit - len;
                    while (len-- > 0) {
                        MarkovFirstWordCount segmentsCount = (MarkovFirstWordCount) session
                                .createQuery("from MarkovFirstWordCount where word = :fw").setString("fw", segment.getLastWord())
                                .uniqueResult();
                        if (segmentsCount == null) {
                            break;
                        }
                        int n = random.nextInt(segmentsCount.getCount().intValue());
                        try {
                            MarkovFirstWord markovFirstWord = (MarkovFirstWord) session
                                    .createQuery("from MarkovFirstWord where word = :fw and number = :n").setEntity("fw", segmentsCount)
                                    .setInteger("n", n).uniqueResult();
                            segment = markovFirstWord.getMarkov();
                            if (!segment.getText().isEmpty()) {
                                result.add(segment.getText());
                            }
                        } catch (NonUniqueResultException e) {
                            log.warn("Non-unique word in the index found: {}/{}.", segmentsCount.getWord(), n);
                        }
                        if (len == 0 && segment.getLastWord().length() < minLastWordLength && leftToHard-- > 0) {
                            len = 1;
                        }
                    }
                    answersLimit--;
                    return SharedUtils.join(result, " ");
                }
            });
        } catch (ClientAuthenticationException e) {
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
}
