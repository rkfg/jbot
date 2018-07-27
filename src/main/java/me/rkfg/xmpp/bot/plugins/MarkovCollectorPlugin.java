package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.NonUniqueResultException;

import me.rkfg.xmpp.bot.domain.Markov;
import me.rkfg.xmpp.bot.domain.MarkovFirstWord;
import me.rkfg.xmpp.bot.domain.MarkovFirstWordCount;
import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;

public class MarkovCollectorPlugin extends MessagePluginImpl {

    private static int SEGMENT_WORDS = 4; // words per segment

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(.+)", Pattern.DOTALL);
    }

    @Override
    public String process(Message message, final Matcher matcher) {
        if (!message.isFromUser()) {
            // don't store system messages
            return null;
        }
        final String text = matcher.group(1).replace('\n', ' ');
        processLine(text);
        return null;
    }

    protected synchronized void processLine(final String text) {
        try {
            HibernateUtil.exec(session -> {
                String words[] = text.split("\\s+");
                if (words.length == 0) {
                    return null;
                }
                int c = 0;
                int pos = 0;
                String segment = null;
                String firstWord = null;
                String lastWord = null;
                do {
                    if (c + SEGMENT_WORDS < words.length) {
                        segment = StringUtils.join(Arrays.copyOfRange(words, c + 1, c + SEGMENT_WORDS), " ");
                        firstWord = purify(words[c]);
                        lastWord = purify(words[c + SEGMENT_WORDS - 1]);
                    } else {
                        if (c + 1 < words.length) {
                            segment = StringUtils.join(Arrays.copyOfRange(words, c + 1, words.length), " ");
                        } else {
                            segment = "";
                        }
                        firstWord = purify(words[c]);
                        lastWord = purify(words[words.length - 1]);
                    }
                    if (segment.length() < 256 && firstWord.length() < 256 && lastWord.length() < 256 && !firstWord.isEmpty()
                            && !lastWord.isEmpty()) {
                        Markov markov = (Markov) session.merge(new Markov(segment, pos, purify(firstWord), purify(lastWord)));
                        try {
                            MarkovFirstWordCount count = (MarkovFirstWordCount) session
                                    .createQuery("from MarkovFirstWordCount where word = :fw").setString("fw", firstWord)
                                    .uniqueResult();
                            if (count == null) {
                                count = (MarkovFirstWordCount) session.merge(new MarkovFirstWordCount(firstWord));
                            }
                            count.incCount();
                            session.merge(new MarkovFirstWord(count, count.getCount() - 1, markov));
                        } catch (NonUniqueResultException e) {
                            log.warn("Non-unique word found: {}", firstWord);
                        }
                        pos++;
                    }
                } while (c++ < words.length - SEGMENT_WORDS);
                return null;
            });
        } catch (GwtUtilException e) {
            log.warn("{}", e);
        }
    }

    public static String purify(String str) {
        return str.toLowerCase().trim().replaceAll("[,.?!;:\"'`«»()\\[\\]{}]", "");
    }
}
