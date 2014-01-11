package me.rkfg.xmpp.bot.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.domain.Markov;

import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.shared.SharedUtils;

public class MarkovResponseCommandPlugin extends MessagePluginImpl {

    Random random = new Random();

    @Override
    public Pattern getPattern() {
        return Pattern.compile(Main.getNick() + "[,: ] ?(.*)");
    }

    @Override
    public String process(final Message message, final Matcher matcher) {
        try {
            return HibernateUtil.exec(new HibernateCallback<String>() {

                @Override
                public String run(Session session) throws LogicException, ClientAuthenticationException {
                    int hash = 0;
                    int off = 0;
                    char val[] = matcher.group(1).toCharArray();
                    for (int i = 0; i < val.length; i++) {
                        hash = 31 * hash + val[off++];
                    }
                    hash = (int) (hash * (System.currentTimeMillis() / 3600000));
                    random.setSeed(hash);
                    Object maxmin[] = (Object[]) session.createQuery("select MAX(id), MIN(id) from Markov").uniqueResult();
                    Long max = (Long) maxmin[0];
                    Long min = (Long) maxmin[1];
                    Markov segment = null;
                    do {
                        segment = (Markov) session.createQuery("from Markov m where m.id = :mid")
                                .setLong("mid", random.nextInt(max.intValue() - min.intValue()) + min).uniqueResult();
                    } while (segment == null);
                    // Markov segment = (Markov) session.createQuery("from Markov m where m.position = 0 order by rand()").setMaxResults(1)
                    // .uniqueResult();
                    List<String> result = new LinkedList<String>();
                    result.add(segment.getFirstWord());
                    int len = random.nextInt(5) + 2;
                    while (len-- > 0) {
                        result.add(segment.getText());
                        segment = (Markov) session.createQuery("from Markov m where m.firstWord = :fw order by rand()")
                                .setString("fw", segment.getLastWord()).setMaxResults(1).uniqueResult();
                        if (segment == null) {
                            break;
                        }
                    }
                    return StringUtils.parseResource(message.getFrom()) + ", " + SharedUtils.join(result, " ");
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

}
