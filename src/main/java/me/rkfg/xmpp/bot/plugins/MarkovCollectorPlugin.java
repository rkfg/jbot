package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.domain.Markov;

import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.shared.SharedUtils;

public class MarkovCollectorPlugin extends MessagePluginImpl {

    private static int SEGMENT_WORDS = 4; // words per segment

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(.+)", Pattern.DOTALL);
    }

    @Override
    public String process(Message message, final Matcher matcher) {
        if (!isMessageFromUser(message)) {
            // don't store system messages
            return null;
        }
        final String text = matcher.group(1).replace('\n', ' ');
        processLine(text);
        return null;
    }

    protected void processLine(final String text) {
        try {
            HibernateUtil.exec(new HibernateCallback<Void>() {

                @Override
                public Void run(Session session) throws LogicException, ClientAuthenticationException {
                    String words[] = text.split("\\s+");
                    int c = 0;
                    int pos = 0;
                    String segment = null;
                    String firstWord = null;
                    String lastWord = null;
                    do {
                        if (c + SEGMENT_WORDS < words.length) {
                            segment = SharedUtils.join(Arrays.copyOfRange(words, c + 1, c + SEGMENT_WORDS), " ");
                            firstWord = words[c];
                            lastWord = words[c + SEGMENT_WORDS - 1];
                        } else {
                            if (c + 1 < words.length) {
                                segment = SharedUtils.join(Arrays.copyOfRange(words, c + 1, words.length), " ");
                            } else {
                                segment = "";
                            }
                            firstWord = words[c];
                            lastWord = words[words.length - 1];
                        }
                        if (segment.length() < 256 && firstWord.length() < 256 && lastWord.length() < 256) {
                            session.merge(new Markov(segment, pos, purify(firstWord), purify(lastWord)));
                            pos++;
                        }
                    } while (c++ < words.length - SEGMENT_WORDS);
                    return null;
                }
            });
        } catch (ClientAuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LogicException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String purify(String str) {
        return str.toLowerCase().replaceAll("[,.?!;:\"'`«»()\\[\\]{}]", "");
    }
}
