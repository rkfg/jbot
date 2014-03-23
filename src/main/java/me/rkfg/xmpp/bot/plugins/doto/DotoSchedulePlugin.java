package me.rkfg.xmpp.bot.plugins.doto;

import me.rkfg.xmpp.bot.plugins.CommandPlugin;
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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * User: violetta
 * Date: 3/20/14
 * Time: 8:47 PM
 */
public class DotoSchedulePlugin extends CommandPlugin
{
    private static final String LINK = "http://www.gosugamers.net/dota2/gosubet";
    private static final int QUERY_LEN = 3;
    private static final String QUERY_LEN_STR = "qls";
    private static final String UPCOMING_MATCHES =  "Upcoming Matches";
    private static final String RECENT_RESULTS = "Recent Results";
    private static final String LIVE_MATCHES = "Live Matches";
    private static final String LIVE_PARAM = "l";
    private static final String UPCOMING_PARAM = "u";
    private static final String RECENT_PARAM = "r";


    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException
    {
        String str = "";
        HashMap<String, Integer> m =parseParams(matcher);
        Document doc;
        try
        {
            doc = getDocument();
        }
        catch(IOException e)
        {
            str = "Эти псы что-то поломали";
            e.printStackTrace();
            return str;
        }
        int num = m.get(QUERY_LEN_STR);
        Elements boxes = doc.select(".box");
        for (Element e : boxes)
        {
            Elements qq = e.select("h2");
            Element q = qq.first();
            if(q.ownText().equals(LIVE_MATCHES) && m.get(LIVE_PARAM) > 0)
            {
                str+= getAll(LIVE_MATCHES, e, num);

            }
            if(q.ownText().equals(UPCOMING_MATCHES) && m.get(UPCOMING_PARAM) > 0)
            {
                str+= getAll(UPCOMING_MATCHES, e, num);
            }
            if(q.ownText().equals(RECENT_RESULTS) && m.get(RECENT_PARAM) > 0)
            {
                str+= getAll(RECENT_RESULTS, e, num);
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
        return "Получить списки матчей с какого-то говносайтика.\n" +
                "Параметры: \n" +
                LIVE_PARAM + " - live matches\n" +
                UPCOMING_PARAM + " - upcoming matches (default)\n"+
                RECENT_PARAM + " - recent matches\n" +
                "n - число строк (default: 3).\n"+
                "Пример: " + PREFIX + "hats r 2";
    }
    private ArrayList<String> getTournamentNames(Element main, int n)
    {
        ArrayList<String>  tNames= new ArrayList<String>();
        Elements tournaments = main.select(".tournament");
        int i=0;
        for(Element t: tournaments)
        {
            if(i==n)
            {
                break;
            }
            String s = t.select(".tooltip-right").first().attr("title");
            Element w = Jsoup.parse(s).select("span").first();

            tNames.add(w.ownText());
            i++;
        }
        return tNames;
    }
    private ArrayList<String> getCSSClass(Element g, String c, int n)
    {
        ArrayList<String> ag = new ArrayList<String>();
        Elements comments = g.select(c);
        int i=0;
        for (Element match: comments)
        {
            if(i == n)
            {
                break;
            }
            ag.add(findText(match));
            i++;
        }
        return ag;
    }
    private ArrayList<String> getComments(Element g, int n)
    {
        return getCSSClass(g, ".type-specific", n);
    }
    private ArrayList<String> getGames(Element g, int n)
    {
        return getCSSClass(g, ".match", n);
    }

    private String findText(Element e)
    {
        String r = "";
        Elements ee = e.children();
        if(ee.size()>0)
        {
            for(Element se : ee)
            {
                if(!se.ownText().equals("") && !se.hasClass("hidden"))
                {
                    r+=se.ownText() + " ";
                    continue;
                }
                r+= findText(se);
            }
        }
        return r;
    }

    private Document getDocument() throws IOException
    {
        Connection c =  Jsoup.connect(LINK);
        c.userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:25.0) Gecko/20100101 Firefox/25.0");
        c.timeout(10000);
        return c.get();
    }
    private HashMap<String, Integer> parseParams(Matcher _matcher)
    {
        HashMap<String, Integer>  m = new HashMap<String, Integer>();
        m.put(LIVE_PARAM, 0);
        m.put(UPCOMING_PARAM, 0);
        m.put(RECENT_PARAM, 0);
        m.put(QUERY_LEN_STR, QUERY_LEN);
        String sss = _matcher.group(2);
        String[] ss = sss.split("\\s+");
        boolean ok = false;
        for (String s: ss){
            if(s.equals(LIVE_PARAM) || s.equals(UPCOMING_PARAM) || s.equals(RECENT_PARAM))
            {
                m.put(s, 1);
                ok = true;
            }
        }
        if(isNumeric(ss[ss.length-1]))
        {
            m.put(QUERY_LEN_STR, Integer.parseInt(ss[ss.length-1]));
        }
        if(!ok)
        {
            m.put(UPCOMING_PARAM, 1);
        }
        return m;
    }
    //!WARNING. Autism.
    private static boolean isNumeric(String str)
    {
        try
        {
           Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
    //Invariant: names.size() == comments.size() == tournaments.size()
    private String format(String title, ArrayList<String> names, ArrayList<String> comments, ArrayList<String> tournaments)
    {

        StringBuilder sb = new StringBuilder();

        sb.append('\n');
        sb.append(title);
        sb.append(":\n");

        if (title.equals(RECENT_RESULTS) || title.equals(UPCOMING_MATCHES))
        {
            for(int i = 0; i< names.size(); i++)
            {
                if(title.equals(RECENT_RESULTS))
                {
                    String s = comments.get(i).replace("Show", "");
                    comments.set(i, s);
                }
                sb.append("[");
                sb.append(comments.get(i));
                sb.append("]  ");
                sb.append(names.get(i));
                sb.append("  [");
                sb.append(tournaments.get(i));
                sb.append("]\n");
            }
        }
        else
        {
            for(int i = 0; i< names.size(); i++)
            {
                sb.append(names.get(i));
                sb.append("  [");
                sb.append(tournaments.get(i));
                sb.append("]\n");
            }
        }
        return sb.toString();
    }
    private String getAll(String id, Element e, int n)
    {
        return format(id, getGames(e, n), getComments(e, n), getTournamentNames(e, n));
    }

}

