package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.domain.Opinion;
import me.rkfg.xmpp.bot.message.BotMessage;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.server.HibernateUtil;

public class OpinionCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(BotMessage message, Matcher matcher) throws GwtUtilException {
        String args = matcher.group(COMMAND_GROUP);
        Matcher whois = Pattern.compile("(\"(.+?)\" = (.+))|((.+?) = (.+))").matcher(args);
        if (!whois.find()) {
            return "неверный формат команды.";
        }
        final String name = whois.group(2) != null ? whois.group(2) : whois.group(5);
        final String opinion = whois.group(3) != null ? whois.group(3) : whois.group(6);
        final String author = message.getNick();
        HibernateUtil.exec(session -> {
            session.createQuery("delete from Opinion where name = :name and author = :author").setString("name", name)
                    .setString("author", author).executeUpdate();
            session.merge(new Opinion(author, name, opinion));
            return null;
        });
        return "записала.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("o", "щ");
    }

    @Override
    public String getManual() {
        return "занести мнение об участнике в БД.\n" + "Формат: <Ник> = <Мнение> (пробелы до и после знака равенства обязательны).\n"
                + "Если ник содержит знак равенства с пробелами вокруг, такой ник можно взять в кавычки.\n" + "Пример: " + PREFIX
                + "o Некто = просто некий достопочтенный господин";
    }
}
