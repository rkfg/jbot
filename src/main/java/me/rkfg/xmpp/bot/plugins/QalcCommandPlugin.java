package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public class QalcCommandPlugin extends CommandPlugin {

    private final int TIMELIMIT = 3;

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        try {
            String inp = matcher.group(COMMAND_GROUP);
            String inpCheck = inp.toLowerCase();
            if (inpCheck.contains("load") || inpCheck.contains("export")) {
                return "команды для работы с файлами заблокированы.";
            }
            Process processQalc = Runtime.getRuntime().exec(new String[] { "qalc", inp });
            int i;
            for (i = 0; i < TIMELIMIT; i++) {
                try {
                    processQalc.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    Thread.sleep(1000);
                }
            }
            if (i == TIMELIMIT) {
                processQalc.destroy();
                return "долго считать.";
            }
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
