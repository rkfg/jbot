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

public class OpinionCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        String args = matcher.group(3);
        Matcher whois = Pattern.compile("(\"(.+?)\" = (.+))|((.+?) = (.+))").matcher(args);
        if (!whois.find()) {
            return "неверный формат команды.";
        }
        final String name = whois.group(2) != null ? whois.group(2) : whois.group(5);
        final String opinion = whois.group(3) != null ? whois.group(3) : whois.group(6);
        final String author = getNick(message);
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthenticationException {
                session.createQuery("delete from Opinion where name = :name and author = :author").setString("name", name)
                        .setString("author", author).executeUpdate();
                session.merge(new Opinion(author, name, opinion));
                return null;
            }
        });
        return "записала.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("o", "щ");
    }

    @Override
    public String getManual() {
        return "занести мнение об участнике в БД.\n"
                + "Формат: <Ник> = <Мнение> (пробелы до и после знака равенства обязательны).\n"
                + "Если ник содержит знак равенства с пробелами вокруг, такой ник можно взять в кавычки.\n"
                + "Пример: " + PREFIX + "o Некто = просто некий достопочтенный господин";
    }
}
