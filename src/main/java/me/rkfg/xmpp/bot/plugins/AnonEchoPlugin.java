package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.jivesoftware.smack.packet.Message;
import org.jxmpp.util.XmppStringUtils;

import me.rkfg.xmpp.bot.Main;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class AnonEchoPlugin extends CommandPlugin {
    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        String s = matcher.group(COMMAND_GROUP);
        Main.sendMUCMessage(s, XmppStringUtils.parseBareJid(message.getFrom()));
        return "Ok";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("e", "echo");
    }

    @Override
    public String getManual() {
        return "Репостить пост в конференцию." + "Пример: " + PREFIX + "e test";
    }
}
