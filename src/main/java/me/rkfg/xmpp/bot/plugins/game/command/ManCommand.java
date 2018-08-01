package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class ManCommand extends AbstractCommand {

    private Map<String, ICommandHandler> handlers;

    public ManCommand(Map<String, ICommandHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public String getCommand() {
        return "ман";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of(args.findFirst().flatMap(arg -> {
            ICommandHandler handler = GamePlugin.findHandler(handlers, arg);
            return handler.getHelp().map(h -> handler.getFormattedCommand() + ": " + h);
        }).orElse("Доступные команды: "
                + handlers.values().stream().map(ICommandHandler::getFormattedCommand).reduce(commaReducer).orElse("команд нет")));
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Получить справку по команде или список всех команд.");
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

    @Override
    public boolean deadAllowed() {
        return true;
    }

}
