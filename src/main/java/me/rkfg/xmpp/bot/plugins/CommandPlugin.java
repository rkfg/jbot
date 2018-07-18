package me.rkfg.xmpp.bot.plugins;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public abstract class CommandPlugin extends MessagePluginImpl {

    protected static final String PREFIX = "%";
    protected static final int COMMAND_NAME_GROUP = 1;
    protected static final int COMMAND_GROUP = 3;
    protected static final int REDIRECT_GROUP = 5;

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^" + PREFIX + "(" + StringUtils.join(getCommand(), "|") + ")( +(.+?)( *> *(.+?) *)?$|$)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public String process(Message message, Matcher matcher) {
        if (!message.isFromUser()) {
            return null;
        }
        try {
            String target = message.getNick();
            if (matcher.group(REDIRECT_GROUP) != null) {
                target = matcher.group(REDIRECT_GROUP);
            }
            final String result = processCommand(message, matcher);
            if (result == null || result.isEmpty()) {
                return null;
            }
            return message.getAppeal(target) + result;
        } catch (final ClientAuthException e) {
            log.warn("{}", e);
        } catch (final LogicException e) {
            log.warn("LogicError while processing command: ", e);
        }
        return "ошибка обработки команды, подробности в логе.";
    }

    public abstract String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException;

    public abstract List<String> getCommand();

    @Override
    public String getManual() {
        return "справка по команде отсутствует.";
    }

}
