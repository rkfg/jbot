package me.rkfg.xmpp.bot.plugins.game.command;

public abstract class AbstractCommand implements ICommandHandler {
    protected String formattedCommand;

    @Override
    public String getFormattedCommand() {
        return formattedCommand;
    }
    
    @Override
    public void setFormattedCommand(String formattedCommand) {
        this.formattedCommand = formattedCommand;
    }
}
