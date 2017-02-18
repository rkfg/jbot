/*
 * Plugin for jbot that fetches random stories from different websites.
 * Copyright (C) 2017 Kona-chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.rkfg.xmpp.bot.plugins;

import org.apache.commons.lang3.ArrayUtils;
import org.jivesoftware.smack.packet.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jsoup.helper.HttpConnection.DEFAULT_UA;

/**
 * Fetches random stories from different websites.
 * At the moment supported websites are: killmepls.ru, ibash.org.ru.
 *
 * @author Kona-chan
 * @version 0.1.0
 */
public final class CoolStoryPlugin extends CommandPlugin {

    private enum Website {

        KILLMEPLS(
                new String[] { "kmp", "кмп" },
                "Получить случайную историю с сайта killmepls.ru.",
                "http://killmepls.ru/random/",
                "div#stories > div.row + div.row > div"
        ),
        IBASH(
                new String[] { "ib", "иб" },
                "Получить случайную историю с сайта ibash.org.ru.",
                "http://ibash.org.ru/random.php",
                "div.quotbody"
        );

        private final String[] commands;
        private final String help;
        private final String urlString;
        private final String cssQuery;

        Website(String[] commands, String help, String urlString, String cssQuery) {
            this.commands = commands;
            this.help = help;
            this.urlString = urlString;
            this.cssQuery = cssQuery;
        }

        private String[] getCommands() {
            return commands;
        }

        private String getHelp() {
            return Stream.of(
                    Arrays.stream(commands)
                            .map(c -> PREFIX + c)
                            .collect(Collectors.joining(", ")),
                    "\t" + help
            ).collect(Collectors.joining("\n"));
        }

        private String getUrlString() {
            return urlString;
        }

        private String getCssQuery() {
            return cssQuery;
        }

    }

    private static final Website[] WEBSITES = new Website[] { Website.KILLMEPLS, Website.IBASH };

    private static final String HELP_AVAILABLE_COMMANDS = "доступные команды:";

    private static final String ERROR_PARSING = "не удалось распарсить страницу с историей.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<String> getCommand() {
        return Arrays.stream(WEBSITES)
                .flatMap(w -> Arrays.stream(w.getCommands()))
                .collect(Collectors.toList());
    }

    @Override
    public String getManual() {
        return Stream.concat(
                Stream.of(HELP_AVAILABLE_COMMANDS),
                Arrays.stream(WEBSITES).map(Website::getHelp)
        ).collect(Collectors.joining("\n"));
    }

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        try {
            final String command = matcher.group(1);

            final Optional<Website> website = Arrays.stream(WEBSITES)
                    .filter(w -> ArrayUtils.contains(w.getCommands(), command))
                    .findFirst();
            if (!website.isPresent()) {
                return null;
            }

            return fetchStory(website.get());

        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    private String fetchStory(Website website) throws IOException {
        final Document doc = Jsoup.connect(website.getUrlString()).userAgent(DEFAULT_UA).get();
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        final Element story = doc.select(website.getCssQuery()).first();
        if (story == null) {
            return ERROR_PARSING;
        }

        story.select("br").after("\\n");
        story.select("p").before("\\n\\n");
        final String storyHtml = story.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(storyHtml, "", Whitelist.none(),
                new Document.OutputSettings().prettyPrint(false));
    }

}