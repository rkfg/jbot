package me.rkfg.xmpp.bot.plugins.doto;

import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import org.apache.commons.cli.*;
import org.jivesoftware.smack.packet.Message;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: violetta
 * Date: 3/20/14
 * Time: 8:47 PM
 */
public class DotoSchedulePlugin extends CommandPlugin
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
    public void init()
    {
        buildOptions();
    }
    void buildOptions()
    {
        Option n = OptionBuilder.hasArg().create(QUERY_LEN_PARAM);
        Option grep = OptionBuilder.hasArg().create(GREP_OPTION);

        opts = new Options();
        opts.addOption(OptionBuilder.create(UPCOMING_PARAM));
        opts.addOption(OptionBuilder.create(LIVE_PARAM));
        opts.addOption(OptionBuilder.create(RECENT_PARAM));
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
            commandLine = parseParams(matcher);
            doc = getDocument(DOTO_BASEURL);
        }
        catch(Exception e)
        {
            str = e.getLocalizedMessage();
            e.printStackTrace();
            return str;
        }

        Elements boxes = doc.select(".box");
        for (Element gameTypeFrame : boxes)
        {
            Elements frameTitle = gameTypeFrame.select("h2");
            if (frameTitle.size() == 0) {
                frameTitle = gameTypeFrame.select("h1");
            }
            Element q = frameTitle.first();
            if (q.ownText().contains(LIVE_MATCHES) && commandLine.hasOption(LIVE_PARAM))
            {
                str+= getAll(LIVE_MATCHES, gameTypeFrame, commandLine);
            }
            if (q.ownText().contains(UPCOMING_MATCHES) && commandLine.hasOption(UPCOMING_PARAM))
            {
                str+= getAll(UPCOMING_MATCHES, gameTypeFrame, commandLine);
            }
            if (q.ownText().contains(RECENT_RESULTS) && commandLine.hasOption(RECENT_PARAM))
            {
                str+= getAll(RECENT_RESULTS, gameTypeFrame, commandLine);
            }
        }
        return str;
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
                GREP_OPTION + " \"regex\" - filter results by regexp" +
                SHOW_STREAMS_OPTION + " - show live stream links"+
                "n - number of matches in output (default: 3).\n"+
                "Example: " + PREFIX + "hats -r -n 2";
    }
    private ArrayList<String> getTournamentNames(Element main)
    {
        ArrayList<String>  tNames= new ArrayList<String>();
        Elements tournaments = main.select(".tournament");
        for(Element t: tournaments)
        {
            String s = t.select(".tooltip-right").first().attr("title");
            Element w = Jsoup.parse(s).select("span").first();
            tNames.add(w.ownText());
        }
        return tNames;
    }
    private ArrayList<String> getCSSClass(Element g, String c)
    {
        ArrayList<String> ag = new ArrayList<String>();
        Elements comments = g.select(c);
        for (Element match: comments)
        {
            ag.add(findText(match));
        }
        return ag;
    }
    private ArrayList<String> getStreamUrl(Element matchTable)
    {
        ArrayList<String> links = new ArrayList<>();
        Elements matches = matchTable.select(".match");
        for (Element match: matches)
        {
            String link = match.attr("href");
            try
            {
                Document doc = getDocument(BASEURL + link);
                Elements streamTabs = doc.select(".match-games-streams .stream-tab-content");
                Elements langs = doc.select(".match-games-streams .lang");
                String streamString = "";
                for (int i =0; i< streamTabs.size();i++)
                {
                    Element streamTab = streamTabs.get(i);
                    String streamTabString = streamTab.toString();
                    String lang  = langs.get(i).text();
                    String streamURL ="";
                    if (streamTabString.contains("twitch"))
                    {
                         Elements flashVars = streamTab.select("[name=flashvars]");
                         String flashVarsStr = flashVars.toString();
                         Pattern pattern = Pattern.compile(".*channel=(.*?)&.*");
                         Matcher m = pattern.matcher(flashVarsStr);
                         m.matches();
                         streamURL = "http://twitch.tv/" + m.group(1);
                    }
                    else
                    {
                        Elements r = streamTab.select("iframe");
                        streamURL = r.attr("src");
                    }
                    streamString += String.format ("\n[%s] %s ", lang, streamURL);
                }
                links.add(streamString);

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return links;
    }
    private ArrayList<String> getComments(Element g)
    {
        return getCSSClass(g, ".type-specific");
    }
    private ArrayList<String> getGames(Element g)
    {
        ArrayList<String> list = new ArrayList<>();
        for (String game: getCSSClass(g, ".match"))
        {
           game = game.replaceAll("\\(\\d+%\\)", "");
           list.add(game);
        }
        return list;
    }

    private String findText(Element e)
    {
        String result = "";
        Elements children = e.children();
        if(children.size()>0)
        {
            for(Element child : children)
            {
                if(!child.ownText().equals("") && !child.hasClass("hidden"))
                {
                    result+=child.ownText() + " ";
                    continue;
                }
                result+= findText(child);
            }
        }
        return result;
    }

    private Document getDocument(String url) throws IOException
    {
        Connection c =  Jsoup.connect(url);
        c.userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
        c.timeout(10000);
        return c.get();
    }
    private CommandLine parseParams(Matcher _matcher) throws ParseException
    {
        String commandParams = _matcher.group(2);
        CommandLineParser clp = new PosixParser();

        return clp.parse(opts, org.apache.tools.ant.types.Commandline.translateCommandline(commandParams));
    }

    private String format(String title, ArrayList<String> names, ArrayList<String> comments, ArrayList<String> tournaments, CommandLine cl)
    {
        StringBuilder sb = new StringBuilder();
        boolean grepSet = cl.hasOption(GREP_OPTION);
        int num = QUERY_LEN;
        if (cl.hasOption(QUERY_LEN_PARAM))
        {
              try
              {
                  num = Integer.parseInt(cl.getOptionValue(QUERY_LEN_PARAM));
              }
              catch(NumberFormatException e){}
        }
        sb.append('\n');
        sb.append(title);
        sb.append(":");

        for(int i = 0; i < names.size() && i< num; i++)
        {
            if(title.equals(RECENT_RESULTS))
            {
                String s = comments.get(i).replace("Show", "");
                comments.set(i, s);
            }
            String s;
            if(title.equals(LIVE_MATCHES) && cl.hasOption(SHOW_STREAMS_OPTION))
            {
                s = String.format("\n%s [%s] %s", names.get(i), tournaments.get(i), comments.get(i));
            }
            else if (title.equals(LIVE_MATCHES))
            {
                s = String.format("\n%s [%s]", names.get(i), tournaments.get(i));
            }
            else
            {
                s = String.format("\n[%s] %s [%s]", comments.get(i), names.get(i), tournaments.get(i));
            }
            if(!grepSet || Pattern.compile(cl.getOptionValue(GREP_OPTION)).matcher(s).find())
            {
                sb.append(s);
            }
        }

        return sb.toString();
    }

    private String getAll(String id, Element e, CommandLine cl)
    {
        if (id.equals(LIVE_MATCHES) && cl.hasOption(SHOW_STREAMS_OPTION))
        {
            return format(id, getGames(e), getStreamUrl(e), getTournamentNames(e), cl);
        }
        return format(id, getGames(e), getComments(e), getTournamentNames(e), cl);
    }

}

