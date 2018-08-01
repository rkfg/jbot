package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;

public class DefaultCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "статус";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        World.THIS.defaultCommand(player);
        return Optional.empty();
    }
    
    @Override
    public Optional<String> getHelp() {
        return Optional.of("Показать ваши характеристики.");
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
