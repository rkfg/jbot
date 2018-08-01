package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class UnknownCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of("неизвестная команда, для просмотра всех доступных команд используйте " + GamePlugin.CMD + "ман");
    }

    @Override
    public Optional<String> getHelp() {
        return exec(null, null);
    }

}
