package me.rkfg.xmpp.bot.plugins.doto;

import de.tobj.twitch.streamobserver.TwitchStreamObserver;
import de.tobj.twitch.streamobserver.channel.event.StreamStatusEvent;
import de.tobj.twitch.streamobserver.channel.event.StreamUpdateEvent;
import de.tobj.twitch.streamobserver.channel.listener.StreamListener;
import me.rkfg.xmpp.bot.Main;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.jivesoftware.smack.packet.Message;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.SettingsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * User: violetta
 * Date: 9/12/15
 * Time: 10:29 AM
 */
public class TwitchTwitcher extends DotoCommandPlugin
{
    private TwitchStreamObserver observer;
    Options opts;
    String client_id;
    ArrayList<String> subscriptions;
    HashMap<String, String> streamStatus;
    private static final String CHANNEL_ADD_PARAM = "a";
    private static final String CHANNEL_REMOVE_PARAM = "d";
    private static final String CHANNELS_SHOW_PARAM = "s";
    private static final String TWITCH_CLIENT_ID = "twitch_id";
    private static final String TWITCH_SUBSCRIPTIONS = "twitch_subscriptions";
    public void init()
    {
        client_id = Main.getSettingsManager().getStringSetting(TWITCH_CLIENT_ID);
        if(client_id == null)
        {
            client_id = "";
        }
        streamStatus = new HashMap<>();
        buildOptions();
        initSubscriptions();
        setupObserver();
    }

    void buildOptions()
    {
        opts = new Options();
        opts.addOption(OptionBuilder.create(CHANNELS_SHOW_PARAM));
        opts.addOption(OptionBuilder.hasArg().create(CHANNEL_ADD_PARAM));
        opts.addOption(OptionBuilder.hasArg().create(CHANNEL_REMOVE_PARAM));
    }
    void initSubscriptions()
    {
        String subs = Main.getSettingsManager().getStringSetting(TWITCH_SUBSCRIPTIONS);
        if(subs == null)
        {
            subs = "";
        }
        subscriptions = new ArrayList<String>(Arrays.asList(subs.split(",\\s?")));
    }

    private void setupObserver()
    {
        observer = new TwitchStreamObserver(client_id);
        for (String sub: subscriptions)
        {
            observer.addChannel(sub);
        }

        observer.addListener(new StreamListener()
        {
            @Override
            public void streamUpdate(StreamUpdateEvent event)
            {
                super.streamUpdate(event);

                String channel = event.getChannelName();
                String newStatus = event.getStreamData().getChannel().getStatus();
                String oldStatus = streamStatus.get(channel);

                if (!newStatus.equals(oldStatus))
                {
                    streamStatus.put(channel, newStatus);
                    String message = String.format("-> Stream http://www.twitch.tv/%s was updated: %s", channel, newStatus);
                    Main.sendMUCMessage(message);
                }
            }

            @Override
            public void streamIsOffline(StreamStatusEvent event)
            {
                super.streamIsOffline(event);
                streamStatus.remove(event.getChannelName());
            }

            @Override
            public void streamIsOnline(StreamStatusEvent event)
            {
                super.streamIsOnline(event);

                String channel = event.getChannelName();
                String status = event.getStreamData().getChannel().getStatus();
                streamStatus.put(channel, status);

                String message = String.format("-> Stream http://www.twitch.tv/%s is up: %s", channel, status);
                Main.sendMUCMessage(message);
            }
        });

        if (subscriptions.size() > 0)
        {
            observer.start();
        }
    }

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException
    {
        CommandLine cl;
        try
        {
            cl = parseParams(opts, matcher);
        }
        catch(InvalidInputException e)
        {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        if (cl.hasOption(CHANNELS_SHOW_PARAM))
        {
             return getSubscriptions(cl);
        }
        if (cl.hasOption(CHANNEL_ADD_PARAM))
        {
            return addSubscription(cl);
        }
        if(cl.hasOption(CHANNEL_REMOVE_PARAM))
        {
             return removeSubscription(cl);
        }
        return "try %man twitch";
    }

    private String getSubscriptions(CommandLine cl)
    {
        String subscriptions = joinSubscriptions();
        if(subscriptions.equals(""))
        {
            return "no subscriptions";
        }
        return subscriptions;
    }

    private String addSubscription(CommandLine cl)
    {
        String channel = cl.getOptionValue(CHANNEL_ADD_PARAM);
        if(!observer.containsChannel(channel))
        {
            subscriptions.add(channel);
            observer.addChannel(channel);
            if(!observer.isAlive())
            {
                observer.start();
            }
            saveSubscriptions();
        }
        return "subscribed to " + channel;
    }

    private String removeSubscription(CommandLine cl)
    {
        String channel = cl.getOptionValue(CHANNEL_REMOVE_PARAM);
        if(observer.containsChannel(channel))
        {
            subscriptions.remove(channel);
            observer.removeChannel(channel);
            saveSubscriptions();
        }
        return "unsubscribed from " + channel;
    }
    private String joinSubscriptions()
    {
        StringBuilder sBuilder = new StringBuilder();

        for (String n : subscriptions) {
            sBuilder.append(n).append(", ");
        }
        if(sBuilder.length() > 0)
        {
            sBuilder.delete(sBuilder.length() - 2, sBuilder.length());
        }
        return sBuilder.toString();
    }

    private void saveSubscriptions()
    {
        SettingsManager sm =  Main.getSettingsManager();
        sm.setStringSetting(TWITCH_SUBSCRIPTIONS, joinSubscriptions());
        try
        {
            sm.saveSettings();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getCommand()
    {
        return Arrays.asList("twitch");
    }

    @Override
    public String getManual()
    {
        String PREFIX_ = PREFIX + "twitch";
        return "Твитчеплагин.\nПараметры:\n" +
               String.format("%s - подписаться на канал; %s -%s example_channel\n", CHANNEL_ADD_PARAM, PREFIX_, CHANNEL_ADD_PARAM) +
               String.format("%s - отписаться от канала; %s -%s example_channel\n", CHANNEL_REMOVE_PARAM, PREFIX_, CHANNEL_REMOVE_PARAM) +
               String.format("%s - показать подписки; %s -%s\n", CHANNELS_SHOW_PARAM, PREFIX_, CHANNELS_SHOW_PARAM);
    }
}
