package me.rkfg.xmpp.bot.plugins.doto;

import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import org.apache.commons.cli.*;

import java.util.regex.Matcher;

/**
 * User: violetta
 * Date: 9/12/15
 * Time: 11:23 AM
 */
public abstract class DotoCommandPlugin extends CommandPlugin
{
    public CommandLine parseParams(Options opts, Matcher matcher) throws InvalidInputException
    {
        String commandParams = matcher.group(2);
        CommandLineParser clp = new PosixParser();
        CommandLine commandLine;
        try
        {
            commandLine = clp.parse(opts, org.apache.tools.ant.types.Commandline.translateCommandline(commandParams));
        }
        catch(ParseException e)
        {
            throw new InvalidInputException(e);
        }
        return commandLine;
    }

    @SuppressWarnings("serial")
    protected class InvalidInputException extends Exception
    {
        InvalidInputException(Exception e)
        {
            super(e);
        }
    }
}
