package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class ManCommand implements ICommandHandler {

    private Map<String, ICommandHandler> handlers;

    public ManCommand(Map<String, ICommandHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("ман");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        return Optional.of(args.findFirst().flatMap(arg -> GamePlugin.findHandler(handlers, arg).getHelp())
                .orElse("Доступные команды (можно использовать только первые буквы): " + handlers.values().stream().distinct()
                        .map(h -> h.getCommand().stream().filter(c -> !c.isEmpty()).reduce((a, c) -> a + "/" + c))
                        .filter(Optional::isPresent).map(Optional::get).reduce(commaReducer).orElse("команд нет")));
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Получить справку по команде или список всех команд.");
    }

    @Override
    public boolean pregameAllowed() {
        return true;
    }

}
