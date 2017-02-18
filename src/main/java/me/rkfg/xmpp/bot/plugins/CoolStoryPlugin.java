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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static org.jsoup.helper.HttpConnection.DEFAULT_UA;

/**
 * Fetches random stories from different websites.
 * At the moment supported websites are: killmepls.ru, ibash.org.ru.
 *
 * @author Kona-chan
 * @version 0.1.0
 */
public final class CoolStoryPlugin extends CommandPlugin {

    private static final String[] KILLMEPLS_COMMANDS = new String[] { "kmp", "кмп" };
    private static final String[] IBASH_COMMANDS = new String[] { "ib", "иб" };
    private static final String[] ENABLED_COMMANDS = ArrayUtils.addAll(KILLMEPLS_COMMANDS, IBASH_COMMANDS);

    private static final String KILLMEPLS_URL = "http://killmepls.ru/random/";
    private static final String KILLMEPLS_CSS_QUERY = "div#stories > div.row + div.row > div";

    private static final String IBASH_URL = "http://ibash.org.ru/random.php";
    private static final String IBASH_CSS_QUERY = "div.quotbody";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        try {
            final String command = matcher.group(1);
            if (ArrayUtils.contains(KILLMEPLS_COMMANDS, command)) {
                return fetchKillMePlsStory();
            }
            if (ArrayUtils.contains(IBASH_COMMANDS, command)) {
                return fetchiBashStory();
            }
            return null;
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList(ENABLED_COMMANDS);
    }

    private String fetchStory(String urlString, String cssQuery) throws IOException {
        final Document doc = Jsoup.connect(urlString).userAgent(DEFAULT_UA).get();

        final Element story = doc.select(cssQuery).first();
        if (story == null) {
            return "не удалось распарсить страницу с историей.";
        }

        return story.text();
    }

    private String fetchKillMePlsStory() throws IOException {
        return fetchStory(KILLMEPLS_URL, KILLMEPLS_CSS_QUERY);
    }

    private String fetchiBashStory() throws IOException {
        return fetchStory(IBASH_URL, IBASH_CSS_QUERY);
    }

}
