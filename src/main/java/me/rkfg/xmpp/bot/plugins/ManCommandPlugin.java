package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.BotMessage;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class ManCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(BotMessage message, Matcher matcher) throws ClientAuthException, LogicException {
        String cmd = matcher.group(COMMAND_GROUP);
        if (cmd == null || cmd.isEmpty()) {
            List<String> commands = new LinkedList<String>();
            for (MessagePlugin plugin : getPlugins()) {
                if (plugin instanceof CommandPlugin) {
                    commands.addAll(((CommandPlugin) plugin).getCommand());
                }
            }
            return "доступные команды: " + StringUtils.join(commands, ", ");
        } else {
            for (MessagePlugin plugin : getPlugins()) {
                if (plugin instanceof CommandPlugin) {
                    if (((CommandPlugin) plugin).getCommand().contains(cmd)) {
                        return plugin.getManual();
                    }
                }
            }
        }
        return "команда не найдена.";
    }

    private List<MessagePlugin> getPlugins() {
        return Main.INSTANCE.getPlugins();
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
