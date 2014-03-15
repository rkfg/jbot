package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public class QalcCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        try {
            Process processQalc = Runtime.getRuntime().exec(new String[] { "qalc", matcher.group(3) });
            processQalc.waitFor();
            BufferedReader resultReader = new BufferedReader(new InputStreamReader(processQalc.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = resultReader.readLine()) != null) {
                if (result.length() > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
            resultReader.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "error executing.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("q", "й");
    }

    @Override
    public String getManual() {
        return "посчитать выражение с помощью qalculate ( http://qalculate.sourceforge.net/features.html )\nФормат: <выражение>\nПример: "
                + PREFIX + "q 2*100 RUB to USD";
    }

}
