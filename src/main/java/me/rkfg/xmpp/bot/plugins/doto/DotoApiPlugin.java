package me.rkfg.xmpp.bot.plugins.doto;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import me.rkfg.xmpp.bot.plugins.doto.json.*;
import org.apache.commons.cli.*;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: violetta
 * Date: 2/1/15
 * Time: 6:51 PM
 */
public class DotoApiPlugin extends CommandPlugin
{
    private final static int BETA = 205790;
    private final static int LIVE = 570;
    private final static String INTERFACE_PREFIX = "IDOTA2Match_";
    private final static String HERO_PREFIX = "IEconDOTA2_";
    private final static String API_BASE_PATH = "https://api.steampowered.com/";
    private final static String API_VERSION = "v1";

    private static final String STEAM_API_KEY_STRING = "steam_api_key";
    private static final int QUERY_LEN = 3;
    private static final String QUERY_LEN_PARAM = "n";
    private static final String LIVE_MATCHES = "Live Matches";

    private static final int LOWEST_LEAGUE_TIER = 3;

    private static final String VERBOSE_PARAM = "v";
    private static final String SHORT_PARAM = "s";
    private static final String PICKS_PARAM = "p";
    private static final String BANS_PARAM = "b";
    private static final String TIER_SELECT_PARAM = "t";
    private static final String GREP_PARAM = "g";

    private static final boolean NUMERIC_TOWERS = true;

    Options opts;
    Map<Integer, String> tierMap;
    private String apikey;
    private Map<Integer, String> heroes;

    public void init()
    {
        buildOptions();
        apikey = Main.getSettingsManager().getStringSetting(STEAM_API_KEY_STRING);
        heroes = getHeroes();
        makeTierRepresentation();
    }

    void buildOptions()
    {
        opts = new Options();
        opts.addOption(OptionBuilder.create(VERBOSE_PARAM));
        opts.addOption(OptionBuilder.create(SHORT_PARAM));
        opts.addOption(OptionBuilder.create(PICKS_PARAM));
        opts.addOption(OptionBuilder.create(BANS_PARAM));
        opts.addOption(OptionBuilder.hasArg().create(QUERY_LEN_PARAM));
        opts.addOption(OptionBuilder.hasArg().create(GREP_PARAM));
        opts.addOption(OptionBuilder.hasArg().create(TIER_SELECT_PARAM));
    }
    private CommandLine parseParams(Matcher _matcher) throws ParseException
    {
        String commandParams = _matcher.group(2);
        CommandLineParser clp = new PosixParser();

        return clp.parse(opts, org.apache.tools.ant.types.Commandline.translateCommandline(commandParams));
    }
    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException
    {
        String str = LIVE_MATCHES;
        try
        {
            str += getLiveLeagueGames(parseParams(matcher));
        }
        catch(Exception e)
        {
            str = e.getLocalizedMessage();
            e.printStackTrace();
        }
        return str;
    }

    private String getLiveLeagueGamesUri()
    {
        return API_BASE_PATH + INTERFACE_PREFIX + LIVE + "/GetLiveLeagueGames/" + API_VERSION + "/?key=" + apikey;
    }

    private String getLeagueListingUri()
    {
        return API_BASE_PATH + INTERFACE_PREFIX + LIVE + "/GetLeagueListing/" + API_VERSION + "/?key=" + apikey;
    }

    private String getHeroesUri()
    {
        return API_BASE_PATH + HERO_PREFIX + LIVE + "/GetHeroes/" + API_VERSION + "/?key=" + apikey;
    }

    private Map<Integer, String> getHeroes()
    {
        Map<Integer, String> m = new HashMap<>();
        try
        {
            JSONObject jo = new JSONObject(sendGet(getHeroesUri()));
            JSONArray heroes = jo.getJSONObject("result").getJSONArray("heroes");
            for(int i = 0; i < heroes.length(); i++)
            {
                JSONObject hero = heroes.getJSONObject(i);
                m.put(hero.getInt("id"), hero.getString("name").replaceFirst("npc_dota_hero_(.*)", "$1"));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return m;
    }

    private void makeTierRepresentation()
    {
        String ONE_TOWER = "┃";
        String TWO_TOWERS = "╏";
        String THREE_TOWERS = "┇";
        tierMap = new HashMap<>();
        tierMap.put(0b111, THREE_TOWERS);
        tierMap.put(0b101, TWO_TOWERS);
        tierMap.put(0b110, TWO_TOWERS);
        tierMap.put(0b011, TWO_TOWERS);
        tierMap.put(0b001, ONE_TOWER);
        tierMap.put(0b010, ONE_TOWER);
        tierMap.put(0b100, ONE_TOWER);
        tierMap.put(0b000, "");
    }

    private String getTierRepresentation(int value)
    {
        return tierMap.get(value);
    }

    private String getBarracksPairState(int b)
    {
        String s = "";
        String BARRACK_MISSING = "-";
        String RANGED_BARRACK = "R";
        String MELEE_BARRACK = "M";

        int ranged = b & 0b10;
        int melee = b & 0b1;

        if(ranged==0)
        {
            s += BARRACK_MISSING;
        }
        else
        {
            s += RANGED_BARRACK;
        }
        if(melee==0)
        {
            s += BARRACK_MISSING;
        }
        else
        {
            s += MELEE_BARRACK;
        }
        return s;
    }

    private String drawTowerStateLinear(int state, int barracks)
    {
        String s = "";
        int top = (state & 1) + (state >> 1 & 1) + (state >> 2 & 1);
        int mid = (state >> 3 & 1) + (state >> 4 & 1) + (state >> 5 & 1);
        int bot = (state >> 6 & 1) + (state >> 7 & 1) + (state >> 8 & 1);
        int t4 = state >> 9;
        if(top > 0 || mid > 0 || bot > 0)
            s += top + "" + mid + bot;
        if(bot==0)
        {
            s += getBarracksPairState(barracks & 0b11);
        }
        if(mid==0)
        {
            s += getBarracksPairState(barracks & 0b1100 >> 2);
        }
        if(top==0)
        {
            s += getBarracksPairState(barracks & 0b110000 >> 4);
        }
        if(bot==0 || top==0 || mid==0)
        {
            s += (t4 & 0b1) + ((t4 & 0b10) >> 1);
        }
        return s;
    }

    private String drawTowerState(int state, boolean dire)
    {

        int t1 = (state & 0b1) | (((state & 0b1000) >> 3) << 1) | (((state & 0b00001000000) >> 6) << 2);
        int t2 = ((state & 0b10) >> 1) | (((state & 0b10000) >> 4) << 1) | (((state & 0b00010000000) >> 7) << 2);
        int t3 = ((state & 0b100) >> 2) | (((state & 0b100000) >> 5) << 1) | (((state & 0b00100000000) >> 8) << 2);
        int t4 = state >> 9;

        String s = "❤" + getTierRepresentation(t4) + getTierRepresentation(t3) +
                getTierRepresentation(t2) + getTierRepresentation(t1);
        if(dire)
        {
            s = new StringBuilder(s).reverse().toString();
        }
        return "❮" + s + "❯";

    }
    private String twoDigitString(int number)
    {
        if(number==0)
        {
            return "00";
        }
        if(number / 10==0)
        {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private String getDurationString(int seconds)
    {
        String s = "";
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        if(hours > 0)
        {
            s += twoDigitString(hours) + "h:";
        }
        return s + twoDigitString(minutes) + "m : " + twoDigitString(seconds) + "s";
    }

    private String getPicks(Team team)
    {
        String s = "";
        for(Pick p : team.getPicks())
        {
            String hero = heroes.get(p.getHeroId());
            if(hero==null)
            {
                break;
            }
            s += hero + ", ";
        }
        return s.substring(0, s.length() - 2);
    }

    private String getBans(Team team)
    {
        String s = "";
        for(Ban p : team.getBans())
        {
            String hero = heroes.get(p.getHeroId());
            if(hero==null)
            {
                break;
            }
            s += hero + ", ";
        }
        return s.substring(0, s.length() - 2);
    }

    private int getSumNetWorth(Team team)
    {
        int sumNetWorth = 0;
        List<Player_> players = team.getPlayers();

        for(Player_ p : players)
        {
            sumNetWorth += p.getNetWorth();
        }
        return sumNetWorth;
    }

    private String getTowerState(Team team, boolean isdire)
    {
        if(NUMERIC_TOWERS)
        {
            return drawTowerStateLinear(team.getTowerState(), team.getBarracksState());
        }
        return drawTowerState(team.getTowerState(), isdire);
    }

    private String handleGame(Game game, CommandLine commandLine)
    {
        String resultString = "";
        int tier = LOWEST_LEAGUE_TIER;
        if (commandLine.hasOption(TIER_SELECT_PARAM))
        {
            try
            {
                tier = Integer.parseInt(commandLine.getOptionValue(TIER_SELECT_PARAM));
            }
            catch(NumberFormatException e){}
        }
        if(game.getLeagueTier() < tier)
        {
            return "";
        }

        String radiant_team_name = "Noname";
        if(null!=game.getRadiantTeam())
            radiant_team_name = game.getRadiantTeam().getTeamName();

        String dire_team_name = "Noname";
        if(null!=game.getDireTeam())
            dire_team_name = game.getDireTeam().getTeamName();

        Scoreboard scoreboard = game.getScoreboard();
        String duration = getDurationString(scoreboard.getDuration().intValue());

        Team radiant = scoreboard.getRadiant();
        Team dire = scoreboard.getDire();

        resultString += String.format("«%s» vs «%s» [%s] [%d:%d]", radiant_team_name, dire_team_name, duration, radiant.getScore(), dire.getScore());
        if(commandLine.hasOption(VERBOSE_PARAM))
        {
            resultString += String.format("[$%d : $%d] [%s / %s]", getSumNetWorth(radiant), getSumNetWorth(dire), getTowerState(radiant, false), getTowerState(dire, true));
        }
        if(commandLine.hasOption(PICKS_PARAM))
        {
            resultString += String.format("\n[%s] vs [%s]", getPicks(radiant), getPicks(dire));
        }
        if(commandLine.hasOption(BANS_PARAM))
        {
            resultString += String.format("\n[%s] vs [%s]", getBans(radiant),getBans(dire));
        }
        return resultString;
    }

    private String getLiveLeagueGames(CommandLine commandLine) throws Exception
    {
        String resultString = "";

        boolean grepSet = commandLine.hasOption(GREP_PARAM);
        int num = QUERY_LEN;
        if (commandLine.hasOption(QUERY_LEN_PARAM))
        {
            try
            {
                num = Integer.parseInt(commandLine.getOptionValue(QUERY_LEN_PARAM));
            }
            catch(NumberFormatException e){}
        }

        String jsonApiAnswerStr = sendGet(getLiveLeagueGamesUri());

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        LiveGames liveGames = om.readValue(jsonApiAnswerStr, LiveGames.class);
        List<Game> games = liveGames.getResult().getGames();

        int count=0;
        for(int i = 0; i < games.size() && count < num; i++)
        {
            Game game = games.get(i);
            try
            {
                String gameInfo = handleGame(game, commandLine);
                if((!grepSet || Pattern.compile(commandLine.getOptionValue(GREP_PARAM)).matcher(gameInfo).find()) && gameInfo.length()!=0)
                {
                    resultString += "\n" + gameInfo;
                    count++;
                }
            }
            catch(NullPointerException e)
            {
                 e.printStackTrace();
            }   //Thanks, Gabe!
            i++;
        }
        return resultString;
    }

    private String sendGet(String url) throws Exception
    {

        String inputLine;
        StringBuffer response = new StringBuffer();

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        while((inputLine = in.readLine())!=null)
        {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    @Override
    public String getManual()
    {
        return  "Live Dota 2 match stats.\n" +
                "Parameters: \n" +
                VERBOSE_PARAM + " - verbose. Output: [Radiant] vs [Dire] [hh:mm:ss] " +
                "[Score] [Net Worth] [top mid bot (num of staying)] [barracks (R or M)] [t4]]\n" +
                SHORT_PARAM + "- short(default). [Radiant] vs [Dire] [hh:mm:ss] [RadiantScore:DireScore]\n" +
                PICKS_PARAM + "- picks\n" +
                BANS_PARAM + "- bans\n" +
                TIER_SELECT_PARAM + "- minimal acceptable tier of match (default: 3)\n"+
                "-grep \"regex\" - filter results by regexp\n" +
                "Example: " + PREFIX + "doto -v -grep Hell";
    }

    @Override
    public List<String> getCommand()
    {
        return Arrays.asList("doto");
    }
}
