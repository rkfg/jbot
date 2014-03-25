package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.Main;

import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.shared.SharedUtils;

public class ManCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        String cmd = matcher.group(COMMAND_GROUP);
        if (cmd == null || cmd.isEmpty()) {
            List<String> commands = new LinkedList<String>();
            for (MessagePlugin plugin : Main.getPlugins()) {
                if (plugin instanceof CommandPlugin) {
                    commands.addAll(((CommandPlugin) plugin).getCommand());
                }
            }
            return "доступные команды: " + SharedUtils.join(commands, ", ");
        } else {
            for (MessagePlugin plugin : Main.getPlugins()) {
                if (plugin instanceof CommandPlugin) {
                    if (((CommandPlugin) plugin).getCommand().contains(cmd)) {
                        return plugin.getManual();
                    }
                }
            }
        }
        return "команда не найдена.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("man", "ьфт", "ман");
    }

    @Override
    public String getManual() {
        return "получить справку по команде.\nФормат: <команда>\nПример: " + PREFIX + "man man";
    }

}
