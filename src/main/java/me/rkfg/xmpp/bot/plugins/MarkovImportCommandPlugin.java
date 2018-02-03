package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public class MarkovImportCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, final Matcher matcher) throws ClientAuthenticationException, LogicException {
        File file = new File(matcher.group(COMMAND_GROUP));
        final MarkovCollectorPlugin markovCollectorPlugin = new MarkovCollectorPlugin();
        int c = 0;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            log.info("File parsing started: {}", file.getCanonicalPath());
            while ((line = reader.readLine()) != null) {
                final String readLine = line;
                executor.submit(() -> markovCollectorPlugin.processLine(readLine));
                c++;
                if (c % 10000 == 0) {
                    log.info("processed {} lines...", c);
                }
            }
            log.info("All lines submitted to the pool, waiting for completion...");
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.DAYS);
            log.info("Import completed.");
        } catch (InterruptedException | IOException e) {
            log.error("Error parsing file: {}", e);
        }
        return "done";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("i", "ш");
    }

}
