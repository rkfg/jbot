package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class RulesCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "правила";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of("Читайте правила на https://rkfg.me/sbr.html");
    }

    @Override
    public boolean deadAllowed() {
        return true;
    }
    
    @Override
    public boolean pregameAllowed() {
        return true;
    }
    
}
