package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import me.rkfg.xmpp.bot.plugins.game.command.AmbushCommand;
import me.rkfg.xmpp.bot.plugins.game.command.AttackCommand;
import me.rkfg.xmpp.bot.plugins.game.command.DefaultCommand;
import me.rkfg.xmpp.bot.plugins.game.command.DescribeCommand;
import me.rkfg.xmpp.bot.plugins.game.command.EquipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.HideCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ICommandHandler;
import me.rkfg.xmpp.bot.plugins.game.command.ListBackpackCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ListPlayersCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ManCommand;
import me.rkfg.xmpp.bot.plugins.game.command.SearchCommand;
import me.rkfg.xmpp.bot.plugins.game.command.SleepCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnequipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UseCommand;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class GamePlugin extends CommandPlugin {

    @Override
    public void init() {
        World.THIS.init();
        setupHandlers();
    }

    private Map<String, ICommandHandler> handlers = new HashMap<>();

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        synchronized (World.THIS) {
            List<String> args = new ArrayList<>();
            final String argsStr = matcher.group(COMMAND_GROUP);
            if (argsStr != null) {
                args = Stream.of(argsStr.split(" ")).filter(c -> !c.isEmpty()).collect(Collectors.toList());
            }
            IPlayer player = World.THIS.getCurrentPlayer(message);
            return processCommand(args, player).orElse("Используйте %гм ман [команда] для получения справки");
        }
    }

    public Optional<String> processCommand(List<String> args, IPlayer player) {
        String cmd = args.stream().findFirst().map(String::toLowerCase).orElse("");
        ICommandHandler f = handlers.get(cmd);
        if (f == null || !f.deadAllowed() && !player.isAlive()) {
            return Optional.empty();
        }
        return Optional.of(f.exec(player, args.stream().skip(1)).orElseGet(player::getLog));
    }

    public void setupHandlers() {
        registerHandler(new DefaultCommand());
        registerHandler(new ListPlayersCommand());
        registerHandler(new SleepCommand());
        registerHandler(new AttackCommand());
        registerHandler(new ListBackpackCommand());
        registerHandler(new EquipCommand());
        registerHandler(new UnequipCommand());
        registerHandler(new SearchCommand());
        registerHandler(new UseCommand());
        registerHandler(new HideCommand());
        registerHandler(new AmbushCommand());
        registerHandler(new DescribeCommand());
        registerHandler(new ManCommand(handlers));
    }

    private void registerHandler(ICommandHandler handler) {
        handler.getCommand().forEach(c -> handlers.put(c, handler));
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("gm", "гм");
    }

}
