package me.rkfg.xmpp.bot.plugins;

import static java.util.Arrays.*;

import java.util.List;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.BotMessage;
import ru.ppsrk.gwt.client.GwtUtilException;

public class LeaveCommandPlugin extends CommandPlugin {

    private String adminName = "";

    @Override
    public void init() {
        getSettingsManager().setDefault("admin", "");
        adminName = getSettingsManager().getStringSetting("admin");
    }
    
    @Override
    public String processCommand(BotMessage message, Matcher matcher) throws GwtUtilException {
        if (message.getFrom().equals(adminName)) {
            sendMessage("Уже ухожу.", message.getFromRoom());
            Main.INSTANCE.leaveRoom(message.getFromRoom());
        }
        return null;
    }

    @Override
    public List<String> getCommand() {
        return asList("leave", "l");
    }
    
}
