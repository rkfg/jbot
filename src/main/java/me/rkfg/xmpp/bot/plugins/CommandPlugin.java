package me.rkfg.xmpp.bot.plugins;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public abstract class CommandPlugin extends MessagePluginImpl {

    protected static final String PREFIX = "%";
    protected static final int COMMAND_GROUP = 3;
    protected static final int REDIRECT_GROUP = 5;

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^" + PREFIX + "(" + StringUtils.join(getCommand(), "|") + ")( +(.+?)( *> *(.+?) *)?$|$)", Pattern.DOTALL);
    }

    @Override
    public String process(Message message, Matcher matcher) {
        if (!isMessageFromUser(message)) {
            return null;
        }
        try {
            String target = getNick(message);
            if (matcher.group(REDIRECT_GROUP) != null) {
                target = matcher.group(REDIRECT_GROUP);
            }
            return getAppeal(message, target) + processCommand(message, matcher);
        } catch (ClientAuthException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LogicException e) {
            log.warn("LogicError while processing command: ", e);
        }
        return "ошибка обработки команды, подробности в логе.";
    }

    public abstract String processCommand(Message message, Matcher matcher) throws LogicException,
            ClientAuthException;

    public abstract List<String> getCommand();
    
    @Override
    public String getManual() {
        return "справка по команде отсутствует.";
    }

}
