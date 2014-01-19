package me.rkfg.xmpp.bot.plugins;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.shared.SharedUtils;

public abstract class CommandPlugin extends MessagePluginImpl {

    private static final String PREFIX = "%";

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^" + PREFIX + "(" + SharedUtils.join(getCommand(), "|") + ")( +(.*)|$)");
    }

    @Override
    public String process(Message message, Matcher matcher) {
        if (!isMessageFromUser(message)){
            return null;
        }
        try {
            return StringUtils.parseResource(message.getFrom()) + ", " + processCommand(message, matcher);
        } catch (ClientAuthenticationException e) {
            e.printStackTrace();
        } catch (LogicException e) {
            log.warn("LogicError while processing command: ", e);
        }
        return "ошибка обработки команды, подробности в логе.";
    }
    
    public abstract String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException;

    public abstract List<String> getCommand();

}
