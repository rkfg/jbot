package me.rkfg.xmpp.bot.plugins.doto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

/**
 * User: violetta
 * Date: 3/20/14
 * Time: 8:47 PM
 */
public class DotoSchedulePlugin extends DotoCommandPlugin
{
    private static final String DOTO_BASEURL = "http://www.gosugamers.net/dota2/gosubet";
    private static final String BASEURL = "http://www.gosugamers.net";
    private static final int QUERY_LEN = 3;
    private static final String QUERY_LEN_PARAM = "n";
    private static final String UPCOMING_MATCHES =  "Upcoming Matches";
    private static final String RECENT_RESULTS = "Recent Results";
    private static final String LIVE_MATCHES = "Live Matches";
    private static final String LIVE_PARAM = "l";
    private static final String UPCOMING_PARAM = "u";
    private static final String RECENT_PARAM = "r";
    private static final String GREP_OPTION = "g";
    private static final String SHOW_STREAMS_OPTION = "s";

    Options opts;

    @Override
    public void init()
    {
        buildOptions();

    }
    @SuppressWarnings("static-access")
    void buildOptions()
    {
        Option n = OptionBuilder.hasArg().create(QUERY_LEN_PARAM);
        Option grep = OptionBuilder.hasArg().create(GREP_OPTION);

        opts = new Options();
        opts.addOption(OptionBuilder.withDescription(UPCOMING_MATCHES).create(UPCOMING_PARAM));
        opts.addOption(OptionBuilder.withDescription(LIVE_MATCHES).create(LIVE_PARAM));
        opts.addOption(OptionBuilder.withDescription(RECENT_RESULTS).create(RECENT_PARAM));
        opts.addOption(OptionBuilder.create(SHOW_STREAMS_OPTION));
        opts.addOption(n);
        opts.addOption(grep);
    }

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException
    {
        String str = "";
        CommandLine commandLine;
        Document doc;
        try
        {
            commandLine = parseParams(opts, matcher);
            doc = getDocument(DOTO_BASEURL);
        }
        catch(InvalidInputException e)
        {
            str = e.getLocalizedMessage();
            log.warn("{}", e);
            return str;
        }
        str = handleBasePage(doc, commandLine);
        return str;
    }
    private String handleBasePage(Document rootPage, CommandLine commandLine)
    {
        StringBuilder result = new StringBuilder();
        Elements boxes = rootPage.select(".box");
        for (Element gameTypeFrame : boxes)
        {
            Elements frameHeader = gameTypeFrame.select("h2");
            if (frameHeader.isEmpty()) {
                frameHeader = gameTypeFrame.select("h1");
            }
            String frameTitle = frameHeader.first().ownText();

            String[] params = {LIVE_PARAM, UPCOMING_PARAM, RECENT_PARAM};
            for (String param: params)
            {
                String description = opts.getOption(param).getDescription();

                if(frameTitle.contains(description) && commandLine.hasOption(param))
                {
                    result.append(format(description, gameTypeFrame, commandLine));
                }
            }
        }
        return result.toString();
    }
    private String format(String title,  Element matchList, CommandLine cl)
    {
        ArrayList<String> names = getTournamentNames(matchList);
        ArrayList<String> games = getGames(matchList);
        ArrayList<String> comments = getMatchComments(title, matchList, cl);

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(title);
        sb.append(":");

        int num = getNumParam(cl);
        for(int i = 0; i < names.size() && i< num; i++)
        {
            String matchDescription;
            if (title.equals(LIVE_MATCHES))
            {
                matchDescription = String.format("%n%s [%s]", games.get(i), names.get(i));

                if(cl.hasOption(SHOW_STREAMS_OPTION))
                {
                    matchDescription += " " + comments.get(i);
                }
            }
            else
            {
                matchDescription = String.format("%n[%s] %s [%s]", comments.get(i), games.get(i), names.get(i));
            }
            if(!cl.hasOption(GREP_OPTION) || Pattern.compile(cl.getOptionValue(GREP_OPTION)).matcher(matchDescription).find())
            {
                sb.append(matchDescription);
            }
        }
        return sb.toString();
    }

    private int getNumParam(CommandLine commandLine)
    {
        int num  = QUERY_LEN;
        if (commandLine.hasOption(QUERY_LEN_PARAM))
        {
            try
            {
                num = Integer.parseInt(commandLine.getOptionValue(QUERY_LEN_PARAM));
            }
            catch(NumberFormatException e) {
                log.warn("{}", e);
            }
        }
        return num;
    }

    private ArrayList<String> getGames(Element g)
    {
        ArrayList<String> list = new ArrayList<>();
        for (String game: getCSSClassOwnText(g, ".match"))
        {
            game = game.replaceAll("\\(\\d+%\\)", "");
            list.add(game);
        }
        return list;
    }

    private ArrayList<String> getTournamentNames(Element main)
    {
        ArrayList<String>  tNames= new ArrayList<>();
        Elements tournaments = main.select(".tournament");
        for(Element tournament: tournaments)
        {
            String s = tournament.select(".tooltip-right").first().attr("title");
            Element tournamentNameWrapper = Jsoup.parse(s).select("span").first();
            tNames.add(tournamentNameWrapper.ownText());
        }
        return tNames;
    }

    private ArrayList<String> getMatchComments(String title,  Element matchList, CommandLine cl)
    {
        ArrayList<String> comments;
        if (title.equals(LIVE_MATCHES) && cl.hasOption(SHOW_STREAMS_OPTION))
        {
            return getStreamUrlsForEachMatch(matchList);
        }

        comments = getComments(matchList);
        if(title.equals(RECENT_RESULTS))
        {
            cleanupRecentResultsComments(comments);
        }
        removeRedundantSpaces(comments);
        return comments;
    }

    private void cleanupRecentResultsComments(ArrayList<String> comments)
    {
        for(int i = 0; i< comments.size(); i++)
        {
            comments.set(i, comments.get(i).replace("Show", ""));
        }
    }
    private void removeRedundantSpaces(ArrayList<String> comments)
    {
        for(int i = 0; i< comments.size(); i++)
        {
            comments.set(i, comments.get(i).trim());
        }
    }
    private ArrayList<String> getComments(Element g)
    {
        return getCSSClassOwnText(g, ".type-specific");
    }

    private ArrayList<String> getStreamUrlsForEachMatch(Element matchTable)
    {
        ArrayList<String> links = new ArrayList<>();
        Elements matches = matchTable.select(".match");
        for (Element match: matches)
        {
            String matchURL = match.attr("href");
            Elements streamTabs = getStreamTabsFromMatchPage(BASEURL + matchURL);
            StringBuilder matchURLs = new StringBuilder();
            for(Element streamTab : streamTabs)
            {
                String streamURLs = makeStreamURL(streamTab);
                matchURLs.append("\n").append(streamURLs);
            }
            links.add(matchURLs.toString());
        }
        return links;
    }

    private Elements getStreamTabsFromMatchPage(String matchURL)
    {
        Elements streamTabs;
        try
        {
            Document doc = getDocument(matchURL);
            streamTabs = doc.select(".matches-streams .match-stream-tab");
        }
        catch(InvalidInputException e)
        {
            log.warn("{}", e);
            streamTabs = new Elements();
        }
        return streamTabs;
    }

    private String makeStreamURL(Element streamTab)
    {
        String streamTabText = streamTab.text();

        String lang  = streamTab.select(".button").text();
        String streamURL = findStreamURL(streamTabText);

        return String.format ("[%s] %s ", lang, streamURL);
    }

    private String findStreamURL(String text)
    {
        String prefix = "";
        Pattern pattern;
        if (text.contains("twitch"))
        {
            pattern = Pattern.compile(".*\\?channel=(.*?)\".*", Pattern.DOTALL);
            prefix =  "http://twitch.tv/";
        }
        else
        {
            pattern = Pattern.compile(".*src=\"(.*?)\".*");
        }
        Matcher m = pattern.matcher(text);
        if(m.matches())
        {
            return prefix + m.group(1);
        }
        return "";
    }

    private ArrayList<String> getCSSClassOwnText(Element element, String className)
    {
        ArrayList<String> al = new ArrayList<>();
        Elements elements = element.select(className);
        for (Element match: elements)
        {
            al.add(findText(match));
        }
        return al;
    }

    private String findText(Element element)
    {
        StringBuilder result = new StringBuilder();
        Elements children = element.children();
        if(!children.isEmpty())
        {
            for(Element child : children)
            {
                if(!child.ownText().equals("") && !child.hasClass("hidden"))
                {
                    result.append(child.ownText()).append(" ");
                    continue;
                }
                result.append(findText(child));
            }
        }
        return result.toString();
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("sch", "hats");
    }

    @Override
    public String getManual()
    {
        return "gosugamers.net parsing plugin.\n" +
                "Parameters: \n" +
                LIVE_PARAM + " - live matches\n" +
                UPCOMING_PARAM + " - upcoming matches (default)\n"+
                RECENT_PARAM + " - recent matches\n" +
                GREP_OPTION + " \"regex\" - filter results by regexp\n" +
                SHOW_STREAMS_OPTION + " - show live stream links\n"+
                "n - number of matches in output (default: 3).\n"+
                "Example: " + PREFIX + "hats -lur -n 2";
    }

    private Document getDocument(String url) throws InvalidInputException
    {
        Connection c =  Jsoup.connect(url);
        c.userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
        c.timeout(10000);
        Document doc;
        try
        {
            doc = c.get();
        }
        catch(IOException e)
        {
            throw new InvalidInputException(e);
        }
        return doc;
    }

}

