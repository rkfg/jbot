package me.rkfg.xmpp.bot.plugins.doto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.SettingsManager;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * User: violetta
 * Date: 3/12/14
 * Time: 8:54 PM
 */
public class V4L3TFollowerPlugin extends CommandPlugin
{
    private static final String OAUTH_CONSUMER_KEY =  "twitter_oauth_consumer_key";
    private static final String OAUTH_CONSUMER_SECRET = "twitter_oauth_consumer_secret";
    private static final String OAUTH_ACCESS_TOKEN = "twitter_oauth_access_token";
    private static final String OAUTH_ACCESS_TOKEN_SECRET = "twitter_oauth_access_token_secret";
        
    private static final String FOLLOW_PARAM = "f";
    private static final String MESSAGE_POST_PARAM = "m";
    private static final String MESSAGE_DEL_PARAM = "d";
    private static final String UNFOLLOW_PARAM = "u";
    private static final String N_PARAM = "n";
    private static final String SHOW_PARAM = "s";
    private static final String ANSWER_PARAM = "a";
    private static final String LIST_PARAM = "l";
    Configuration config;
    Options opts;
    Twitter twitter;
    @SuppressWarnings("static-access")
    void buildOptions()
    {
        opts = new Options();

        String[] params =  new String[]{FOLLOW_PARAM, MESSAGE_POST_PARAM, UNFOLLOW_PARAM, SHOW_PARAM, N_PARAM, ANSWER_PARAM};
        for(String s : params)
        {
            opts.addOption(OptionBuilder.hasArg().withArgName(s).create(s));
        }

        Option n = OptionBuilder.create(MESSAGE_DEL_PARAM);
        opts.addOption(n);
        Option a = OptionBuilder.create(LIST_PARAM);
        opts.addOption(a);

    }

    @Override
    public void init()
    {
        configure();
        buildOptions();
        twitter = new TwitterFactory(config).getInstance();
        TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
        twitterStream.addListener(listener);
        // user() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.user();
    }
    private void configure()
    {
        ConfigurationBuilder cb= new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        SettingsManager sm = getSettingsManager();
        cb.setOAuthConsumerKey(sm.getStringSetting(OAUTH_CONSUMER_KEY));
        cb.setOAuthConsumerSecret(sm.getStringSetting(OAUTH_CONSUMER_SECRET));
        cb.setOAuthAccessToken(sm.getStringSetting(OAUTH_ACCESS_TOKEN));
        cb.setOAuthAccessTokenSecret(sm.getStringSetting(OAUTH_ACCESS_TOKEN_SECRET));
        config = cb.build();
    }
    UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onStatus(Status status) {
            String w = "@" + status.getUser().getScreenName() + " - " + status.getText();
            sendMUCMessage(w);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            System.out.println("Got a direct message deletion notice id:" + directMessageId);
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses){}
        @Override
        public void onScrubGeo(long userId, long upToStatusId){}
        @Override
        public void onStallWarning(StallWarning warning){}
        @Override
        public void onFriendList(long[] friendIds){}
        @Override
        public void onFavorite(User source, User target, Status favoritedStatus){}
        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus){}
        @Override
        public void onFollow(User source, User followedUser){}
        @Override
        public void onUnfollow(User user, User user2){}

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            System.out.println("onDirectMessage text:"
                    + directMessage.getText());
        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list){}
        @Override
        public void onUserListMemberDeletion(User user, User user2, UserList userList){}
        @Override
        public void onUserListSubscription(User user, User user2, UserList userList){}
        @Override
        public void onUserListUnsubscription(User user, User user2, UserList userList){}
        @Override
        public void onUserListCreation(User user, UserList userList){}
        @Override
        public void onUserListUpdate(User user, UserList userList){}
        @Override
        public void onUserListDeletion(User user, UserList userList){}
        @Override
        public void onUserProfileUpdate(User updatedUser){}
        @Override
        public void onBlock(User user, User user2){}
        @Override
        public void onUnblock(User user, User user2){}
        @Override
        public void onException(Exception e){}
    };

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException
    {
        String s = "";
        HashMap<String, String> hm= parseParams(matcher);
        int n  = 3;
        if(hm.get(N_PARAM)!=null)
        {
            try
            {
                n = Integer.parseInt(hm.get(N_PARAM));
            }
            catch(NumberFormatException e){s+= e.getLocalizedMessage() + "НЕТ ЧИСЛА\n";}
        }
        if (hm.get(MESSAGE_POST_PARAM) != null && hm.get(ANSWER_PARAM) == null)
        {
            post(hm.get(MESSAGE_POST_PARAM));
        }
        if(hm.get(MESSAGE_DEL_PARAM)!=null)
        {
            s+=removeLastStatus();
        }
        if(hm.get(SHOW_PARAM)!= null)
        {
            s += show(hm.get(SHOW_PARAM), n);
        }
        if(hm.get(FOLLOW_PARAM)!= null)
        {
            s+=follow(hm.get(FOLLOW_PARAM));
        }
        if(hm.get(UNFOLLOW_PARAM)!= null)
        {
            s+=unfollow(hm.get(UNFOLLOW_PARAM));
        }
        if(hm.get(ANSWER_PARAM)!= null)
        {
            s+=answer(hm.get(MESSAGE_POST_PARAM), hm.get(ANSWER_PARAM));
        }
        if(hm.get(LIST_PARAM)!= null)
        {
            s+=list(n);
        }

        if (s.isEmpty())
        {
            return "Done";
        }
        return s;
    }

    private String answer(String s, String _id)
    {
        long id = Long.parseLong(_id);
        String ss ="";
        Status status = null;
        try
        {
             status = twitter.showStatus(id);
        }
        catch(TwitterException e){ss+=e.getMessage();}
        StatusUpdate stat = new StatusUpdate("@" + status.getUser().getScreenName() + " " + s);
        stat.setInReplyToStatusId(id);
        try
        {
            twitter.updateStatus(stat);
        }
        catch(TwitterException e)
        {
            ss+=e.getMessage();
        }
        return ss;
    }

    private String list(int n)
    {
        String s = "";
        List<Status> statuses = null;
        try
        {
            statuses = twitter.getHomeTimeline();
        }
        catch(TwitterException e)
        {
            s+=e.getLocalizedMessage()+"\n";
        }
        for (int i = 0; i<n && i<statuses.size(); i++)
        {
            s+=statuses.get(i).getText() + "["+statuses.get(i).getId()+"]\n";
        }
        return s;
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("tw", "valet");
    }

    @Override
    public String getManual() {
        String REFIX = PREFIX+"valet";
        return "Плагин управления Валетом. \n"+
        "Параметры: \n" +
        MESSAGE_POST_PARAM + " - запостить \"сообщение\"; "+REFIX + " -" +MESSAGE_POST_PARAM +" \"Чпок\""+"\n"+
        MESSAGE_DEL_PARAM + " - удалить последний пост; "+REFIX + " -" +MESSAGE_DEL_PARAM+"\n"+
        FOLLOW_PARAM + " - зафоловить username; "+REFIX+ " -"+FOLLOW_PARAM+ " valet" +"\n"+
        UNFOLLOW_PARAM + " - расфоловить cocksucker; " + REFIX+ " -"+UNFOLLOW_PARAM+ " valet" +"\n"+
        N_PARAM + " - число; влияет на другие команды\n"+
        SHOW_PARAM + " - показать n последних постов username; "+REFIX+ " -"+SHOW_PARAM+ " valet" +"\n"+
        LIST_PARAM + " - показать n последних пришедших постов и их [id] "+REFIX + " -" +LIST_PARAM+"\n"+
        ANSWER_PARAM + " - ответить сообщением параметра -" + MESSAGE_POST_PARAM + " на пост с id параметра -" + ANSWER_PARAM+"; "
                +REFIX + " -" +MESSAGE_POST_PARAM +" \"Чпок\" -"+ ANSWER_PARAM+ " 1335995331"+"\n";
    }
    private HashMap<String, String> parseParams(Matcher _matcher)
    {

        HashMap<String, String>  m = new HashMap<String, String>();
        m.put(MESSAGE_POST_PARAM, null);
        m.put(MESSAGE_DEL_PARAM, null);
        m.put(FOLLOW_PARAM, null);
        m.put(UNFOLLOW_PARAM, null);

        String sss = _matcher.group(2);
    
        String[] params = new String[]{MESSAGE_POST_PARAM, MESSAGE_DEL_PARAM, FOLLOW_PARAM, UNFOLLOW_PARAM, SHOW_PARAM, N_PARAM, ANSWER_PARAM};
        CommandLineParser clp = new GnuParser();
        String []ss =  org.apache.tools.ant.types.Commandline.translateCommandline(sss);
        try
        {
            CommandLine cl = clp.parse(opts, ss);
            for (String option: params)
            {
                if(cl.hasOption(option))
                {
                    m.put(option, cl.getOptionValue(option));
                }
            }
            if(cl.hasOption(MESSAGE_DEL_PARAM))
            {
                m.put(MESSAGE_DEL_PARAM, "");
            }
            if(cl.hasOption(LIST_PARAM))
            {
                m.put(LIST_PARAM, "");
            }
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }

        return m;
    }
    private String show(String username, int num)
    {
        String s = "";
        try {
            List<Status> statuses;
            statuses = twitter.getUserTimeline(username);
            s+= "Showing @" + username + "'s user timeline. (" + twitter.showUser(username).getDescription()+")\n";
            for (int i =0; i<num && i<statuses.size(); i++)
            {
                s+= statuses.get(i).getText()+ "\n";
            }
        } catch (TwitterException te) {
            te.printStackTrace();
        }
        return s;
    }
    private String follow(String username)
    {
        String s = "";
        try
        {
            twitter.createFriendship(username);
        }
        catch(TwitterException e)
        {
            s+=e.getLocalizedMessage()+"\n";
        }
        return s+"Following @" + username;
    }
    private String unfollow(String username)
    {
        String s = "";
        try
        {
            twitter.destroyFriendship(username);
        }
        catch(TwitterException e)
        {
            s+=e.getLocalizedMessage()+"\n";
        }
        return s+"Unfollowed @" + username;
    }
    private String removeLastStatus()
    {
        String s = "";
        List<Status> statuses = null;
        try
        {
             statuses = twitter.getUserTimeline();
             twitter.destroyStatus(statuses.get(0).getId());
             s = "REMOVED: " + statuses.get(0).getText();
        }
        catch(TwitterException e)
        {
            s=e.getLocalizedMessage();
        }
        return s;
    }
    public String post(String post)
    {
        String s = "";
        try
        {
            twitter.updateStatus(post);
        }
        catch(TwitterException e)
        {
            s+=e.getLocalizedMessage()+"\n";
        }
        return s;
    }

}
