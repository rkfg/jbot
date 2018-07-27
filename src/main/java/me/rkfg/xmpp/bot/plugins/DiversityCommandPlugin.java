package me.rkfg.xmpp.bot.plugins;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;

public class DiversityCommandPlugin extends CommandPlugin {

    private static int PRECISION = 5;

    @Override
    public String processCommand(Message message, final Matcher matcher) throws GwtUtilException {
        return HibernateUtil.exec(session -> {
            double precCoeff = Math.pow(10, PRECISION);
            try {
                double power = Double.parseDouble(matcher.group(2));
                if (power < 10 && power > 0 && power == Math.round(power)) {
                    precCoeff = Math.pow(10, power);
                }
            } catch (NumberFormatException e) {
                // okay
            }
            Long segmentsCount = (Long) session.createQuery("select count(*) from Markov").uniqueResult();
            BigInteger joinsCount = (BigInteger) session.createSQLQuery("select count(*) from sequence_join").uniqueResult();
            return String.valueOf(Math.round(precCoeff * joinsCount.doubleValue() / segmentsCount) / precCoeff);
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("div", "вшм", "див");
    }
}
