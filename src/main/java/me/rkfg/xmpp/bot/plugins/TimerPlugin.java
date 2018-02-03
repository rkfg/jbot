package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jxmpp.util.XmppStringUtils;

import me.rkfg.xmpp.bot.message.Message;

public class TimerPlugin extends CommandPlugin
{
    private static final String DAY_OPTION = "d";
    private static final String HOUR_OPTION = "h";
    private static final String MIN_OPTION = "m";
    private static final String SEC_OPTION = "s";
    private static final String MESSAGE_OPTION = "a";
    String[] params = new String[]{DAY_OPTION, HOUR_OPTION, MIN_OPTION, MESSAGE_OPTION, SEC_OPTION};
    Options opts;
    Timer timer;
    class TimerTaskE extends TimerTask{

        String msg = "";
        String to = "";
        TimerTaskE(String s, String to)
        {
            super();
            msg = s;
            this.to = to;
        }
        @Override
        public void run()
        {
            sendMUCMessage(msg, to);
        }
    };
    @Override
    public void init()
    {
        timer = new Timer(true);
        buildOptions();
    }
    @Override
    public String processCommand(Message message, Matcher matcher)
    {
        String s = "";
        HashMap<String, String> hm= parseParams(matcher);
        long days = 0;
        long mins = 0;
        long hours = 0;
        long secs = 0;
        String msg="";
        try
        {
            if(hm.get(DAY_OPTION)!=null)
            {
                days = Integer.parseInt(hm.get(DAY_OPTION));
            }
            if(hm.get(HOUR_OPTION)!=null)
            {
                hours = Integer.parseInt(hm.get(HOUR_OPTION));
            }
            if(hm.get(SEC_OPTION)!=null)
            {
               secs = Integer.parseInt(hm.get(SEC_OPTION));
            }
            if(hm.get(MIN_OPTION)!=null)
            {
                mins = Integer.parseInt(hm.get(MIN_OPTION));

            }
            if(hm.get(MESSAGE_OPTION)!=null)
            {
                msg = hm.get(MESSAGE_OPTION);
            }
            else
            {
                msg = "ты просил напомнить что-то. Вот.";
            }
            long result = (mins + (hours + days*24)*60)*60 + secs;
            if(result<=0)
            {
                throw new Exception("Время не может пойти взад!");
            }
            TimerTask tt = new TimerTaskE(XmppStringUtils.parseResource(message.getFrom())+": "+ msg, XmppStringUtils.parseBareJid(message.getFrom()));
            timer.schedule(tt, result*1000);
        }
        catch(Exception e){s+=e.getMessage();}
        if (s.isEmpty())
        {
            return "Ok";
        }
        return s;
    }

    @Override
    public List<String> getCommand()
    {
        return Arrays.asList("t", "timer");
    }
    @Override
    public String getManual() {
        String REFIX = PREFIX+"timer";
        return "Плагин-таймер. \n"+
                "Параметры: \n" +
                DAY_OPTION + " - дни; "+REFIX + " -" + DAY_OPTION + " " + 30+"\n"+
                HOUR_OPTION + " - часы; "+REFIX + " -" + HOUR_OPTION + " " + 10 +"\n"+
                MIN_OPTION + " - минуты; "+REFIX+ " -"+MIN_OPTION+ " " +30 +"\n"+
                SEC_OPTION + " - секунды; "+REFIX+ " -"+SEC_OPTION+ " " +42 +"\n"+
                MESSAGE_OPTION + " - запостить \"сообщение\"; "+REFIX + " -" +MESSAGE_OPTION +" \"Борщ готов!\"";
    }
    @SuppressWarnings("static-access")
    void buildOptions()
    {
        opts = new Options();
        for(String s : params)
        {
            opts.addOption(OptionBuilder.hasArg().withArgName(s).create(s));
        }
    }
    private HashMap<String, String> parseParams(Matcher _matcher)
    {

        HashMap<String, String>  m = new HashMap<String, String>();
        m.put(DAY_OPTION, null);
        m.put(HOUR_OPTION, null);
        m.put(MIN_OPTION, null);
        m.put(MESSAGE_OPTION, null);
        m.put(SEC_OPTION, null);
        String sss = _matcher.group(2);


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
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }

        return m;
    }

}
