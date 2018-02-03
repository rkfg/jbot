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

import static org.jsoup.helper.HttpConnection.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

/**
 * Fetches random stories from different websites.
 * At the moment supported websites are: killmepls.ru, ibash.org.ru, anekdot.ru, svalko.org.
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
        ),
        ANEKDOTRU(
                new String[] { "aru", "ару" },
                "Получить случайный анекдот с сайта anekdot.ru.",
                "https://www.anekdot.ru/random/anekdot/",
                "div.content > div > div + div > div.text"
        ),
        SVALKO(
                new String[] { "sv", "св" },
                "Получить случайный пост с сайта svalko.org",
                "http://svalko.org/random.html",
                "div#content > div.single > div.text"
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

    private static final Website[] WEBSITES = new Website[] {
            Website.KILLMEPLS,
            Website.IBASH,
            Website.ANEKDOTRU,
            Website.SVALKO,
    };

    private static final boolean CONFIG_REROLL_LONG_STORIES = true;
    private static final int CONFIG_MAX_STORY_LENGTH = 4096;
    private static final int CONFIG_MAX_STORY_LINES = 5;
    private static final int CONFIG_MAX_ROLLS = 5;

    private static final String HELP_AVAILABLE_COMMANDS = "доступные команды:";

    private static final String ERROR_WEBSITE_NOT_SUPPORTED = "сайт в данный момент не поддерживается.";
    private static final String ERROR_COULD_NOT_PARSE = "не могу распарсить ответ от сервера.";

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
                    .filter(w -> ArrayUtils.contains(w.getCommands(), command.toLowerCase()))
                    .findFirst();
            if (!website.isPresent()) {
                return ERROR_WEBSITE_NOT_SUPPORTED;
            }

            return fetchStory(website.get());

        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    private String fetchStory(Website website) throws IOException {
        int roll = 0;
        String result;
        int resultLength;
        int resultLines;

        //noinspection ConstantConditions
        do {
            roll++;

            final Document doc = Jsoup.connect(website.getUrlString()).userAgent(DEFAULT_UA).get();
            doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
            logger.info("Fetched a story from {}", doc.location());

            final Element story = doc.select(website.getCssQuery()).first();
            if (story == null) {
                return ERROR_COULD_NOT_PARSE;
            }

            story.select("div").remove();
            story.select("img").forEach(
                    img -> img.replaceWith(new TextNode(img.attr("src"), ""))
            );
            story.select("br").after("\\n");
            story.select("p").before("\\n\\n");
            final String storyHtml = story.html().replaceAll("\\\\n", "\n");

            result = Jsoup.clean(storyHtml, "", Whitelist.none(),
                    new Document.OutputSettings().prettyPrint(false)).trim();
            resultLength = result.length();
            resultLines = countLines(result);

        } while (CONFIG_REROLL_LONG_STORIES
                && (resultLength > CONFIG_MAX_STORY_LENGTH || resultLines > CONFIG_MAX_STORY_LINES)
                && roll <= CONFIG_MAX_ROLLS);

        return result;
    }

    private int countLines(String string) {
        final Matcher matcher = Pattern.compile("(\r\n)|(\r)|(\n)").matcher(string);
        int lines = 0;
        while (matcher.find()) {
            lines++;
        }
        return lines;
    }

}
