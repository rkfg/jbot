package me.rkfg.xmpp.bot.plugins;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;

public class DiversityCommandPlugin extends CommandPlugin {

    private static int PRECISION = 5;

    @Override
    public String processCommand(Message message, final Matcher matcher) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<String>() {

            @Override
            public String run(Session session) throws LogicException, ClientAuthException {
                double prec_coeff = Math.pow(10, PRECISION);
                try {
                    double power = Double.valueOf(matcher.group(2));
                    if (power < 10 && power > 0 && power == Math.round(power)) {
                        prec_coeff = Math.pow(10, power);
                    }
                } catch (NumberFormatException e) {
                    // okay
                }
                Long segmentsCount = (Long) session.createQuery("select count(*) from Markov").uniqueResult();
                BigInteger joinsCount = (BigInteger) session.createSQLQuery("select count(*) from sequence_join").uniqueResult();
                return String.valueOf(Math.round(prec_coeff * joinsCount.doubleValue() / segmentsCount) / prec_coeff);
            }
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("div", "вшм", "див");
    }
}
