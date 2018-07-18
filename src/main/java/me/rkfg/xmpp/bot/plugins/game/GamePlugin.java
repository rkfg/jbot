package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import me.rkfg.xmpp.bot.plugins.game.command.AmbiguousCommand;
import me.rkfg.xmpp.bot.plugins.game.command.AmbushCommand;
import me.rkfg.xmpp.bot.plugins.game.command.AttackCommand;
import me.rkfg.xmpp.bot.plugins.game.command.DefaultCommand;
import me.rkfg.xmpp.bot.plugins.game.command.DescribeCommand;
import me.rkfg.xmpp.bot.plugins.game.command.DropItemCommand;
import me.rkfg.xmpp.bot.plugins.game.command.EquipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.HideCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ICommandHandler;
import me.rkfg.xmpp.bot.plugins.game.command.ListBackpackCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ListPlayersCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ManCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ParticipatingCommand;
import me.rkfg.xmpp.bot.plugins.game.command.ReadyCommand;
import me.rkfg.xmpp.bot.plugins.game.command.RebuildItemsCommand;
import me.rkfg.xmpp.bot.plugins.game.command.SearchCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnequipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnknownCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UseCommand;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
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
            Optional<IPlayer> player = World.THIS.getCurrentPlayer(message);
            List<String> argsf = args;
            return player.map(p -> processCommand(argsf, p).orElse("Используйте %гм ман [команда] для получения справки"))
                    .orElse("Игра уже идёт, и вы в ней не участвуете.");
        }
    }

    public Optional<String> processCommand(List<String> args, IPlayer player) {
        String cmd = args.stream().findFirst().map(String::toLowerCase).orElse("");
        ICommandHandler f = findHandler(handlers, cmd);
        if (!f.deadAllowed() && !player.isAlive() && World.THIS.getState() == GamePlayerState.PLAYING) {
            return Optional.empty();
        }
        if (World.THIS.getState() != GamePlayerState.PLAYING && !f.pregameAllowed()) {
            return Optional.of("Эта команда не разрешена вне игры.");
        }
        return Optional.of(f.exec(player, args.stream().skip(1)).orElseGet(player::getLog));
    }

    public static ICommandHandler findHandler(Map<String, ICommandHandler> handlers, String cmd) {
        ICommandHandler f = handlers.get(cmd);
        if (f == null) {
            for (Entry<String, ICommandHandler> entry : handlers.entrySet()) {
                if (entry.getKey().startsWith(cmd)) {
                    if (f != null) {
                        f = new AmbiguousCommand();
                    } else {
                        f = entry.getValue();
                    }
                }
            }
        }
        if (f == null) {
            f = new UnknownCommand();
        }
        return f;
    }

    public void setupHandlers() {
        registerHandler(new DefaultCommand());
        registerHandler(new ListPlayersCommand());
        registerHandler(new AttackCommand());
        registerHandler(new ListBackpackCommand());
        registerHandler(new EquipCommand());
        registerHandler(new UnequipCommand());
        registerHandler(new SearchCommand());
        registerHandler(new UseCommand());
        registerHandler(new HideCommand());
        registerHandler(new AmbushCommand());
        registerHandler(new DescribeCommand());
        registerHandler(new ParticipatingCommand());
        registerHandler(new ReadyCommand());
        registerHandler(new DropItemCommand());
        registerHandler(new RebuildItemsCommand());
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
