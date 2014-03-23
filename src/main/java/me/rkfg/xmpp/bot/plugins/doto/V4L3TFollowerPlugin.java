package me.rkfg.xmpp.bot.plugins.doto;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import org.jivesoftware.smack.packet.Message;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

/**
 * User: violetta
 * Date: 3/12/14
 * Time: 8:54 PM
 */
public class V4L3TFollowerPlugin extends CommandPlugin
{
    private static final String OAUTH_CONSUMER_KEY =  "";
    private static final String OAUTH_CONSUMER_SECRET = "";
    private static final String OAUTH_ACCESS_TOKEN = "";
    private static final String OAUTH_ACCESS_TOKEN_SECRET = "";
    @Override
    public void init()
    {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(OAUTH_CONSUMER_KEY);
        cb.setOAuthConsumerSecret(OAUTH_CONSUMER_SECRET);
        cb.setOAuthAccessToken(OAUTH_ACCESS_TOKEN);
        cb.setOAuthAccessTokenSecret(OAUTH_ACCESS_TOKEN_SECRET);
        TwitterStream twitterStream = new TwitterStreamFactory( cb.build()).getInstance();
        twitterStream.addListener(listener);
        // user() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.user();
    }
    static UserStreamListener listener = new UserStreamListener() {
        @Override
        public void onStatus(Status status) {
            String w = "@" + status.getUser().getScreenName() + " - " + status.getText();
            Main.sendMUCMessage(w);
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
        String str = "gay";
        return str;
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("tw", "valet");
    }

    @Override
    public String getManual() {
        return "фолловнуть @twittername.\n" + "Пример: " + PREFIX + "t @gay";
    }
}

