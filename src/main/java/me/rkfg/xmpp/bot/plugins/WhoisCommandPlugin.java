package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.domain.Opinion;

import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;

public class WhoisCommandPlugin extends CommandPlugin {

    @Override
    public synchronized String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        final Matcher cmd = Pattern.compile("(a|n|o) (.+)").matcher(matcher.group(COMMAND_GROUP));
        if (!cmd.find()) {
            return "неверные параметры команды.";
        }
        return HibernateUtil.exec(new HibernateCallback<String>() {

            @Override
            public String run(Session session) throws LogicException, ClientAuthenticationException {
                String paramName = "name";
                String intro = "вот что говорят про %s";
                String negative = "ничего не говорят про %s";
                String type = cmd.group(1);
                String paramValue = cmd.group(2);
                if (type.equals("a")) {
                    paramName = "author";
                    intro = "вот как отзывается %s";
                    negative = "никак не отзывается %s";
                }
                if (type.equals("o")) {
                    paramName = "opinion";
                    intro = "вот о ком говорят «%s»";
                    negative = "ни о ком не говорят «%s»";
                }
                @SuppressWarnings("unchecked")
                List<Opinion> opinions = session.createQuery("from Opinion where " + paramName + " like :param order by id desc")
                        .setString("param", "%" + paramValue + "%").setMaxResults(10).list();
                if (opinions.size() == 0) {
                    return String.format(negative, paramValue);
                }
                StringBuilder result = new StringBuilder();
                result.append(String.format(intro, paramValue)).append('\n');
                for (Opinion opinion : opinions) {
                    result.append(String.format("%s: «%s — %s»\n", opinion.getAuthor(), opinion.getName(), opinion.getOpinion()));
                }
                return result.toString();
            }
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("w", "ц");
    }

    @Override
    public String getManual() {
        return "получить мнения из БД.\n"
                + "Формат: "
                + PREFIX
                + "w <a|n|o> <Текст>\na — поиск по автору, n — по имени, o — по тексту мнения. Выдаются все совпадения с указанным текстом (не с отдельными словами).\n"
                + "Пример: " + PREFIX + "w n нект";
    }
}
