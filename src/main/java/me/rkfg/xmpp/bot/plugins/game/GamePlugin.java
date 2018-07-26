package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.MessagePluginImpl;
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
import me.rkfg.xmpp.bot.plugins.game.command.SpendPointsCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnequipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnknownCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UseCommand;
import me.rkfg.xmpp.bot.plugins.game.command.WhisperCommand;
import me.rkfg.xmpp.bot.plugins.game.command.YellCommand;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;

public class GamePlugin extends MessagePluginImpl {

    public static final String CMD = ""; // may be set to '%gm ' if the bot runs other plugins

    @Override
    public void init() {
        World.THIS.init("data");
        setupHandlers();
    }

    private Map<String, ICommandHandler> handlers = new HashMap<>();

    @Override
    public String process(Message message, Matcher matcher) {
        synchronized (World.THIS) {
            List<String> args = new ArrayList<>();
            final String argsStr = matcher.group(1);
            if (argsStr != null) {
                args = Stream.of(argsStr.split(" ")).filter(c -> !c.isEmpty()).collect(Collectors.toList());
            }
            Optional<IPlayer> player = World.THIS.getCurrentPlayer(message);
            List<String> argsf = args;
            if (!player.isPresent()) {
                log.debug("Not present: f: {}, r: {}, b: {}", message.getFrom(), message.getFromRoom(), message.getBody());
                return "Игра уже идёт, и вы в ней не участвуете.";
            }
            return player.flatMap(p -> processCommand(argsf, p)).orElseGet(() -> {
                World.THIS.flushLogs();
                return null;
            });
        }
    }

    public Optional<String> processCommand(List<String> args, IPlayer player) {
        String cmd = args.stream().findFirst().map(String::toLowerCase).orElse("");
        ICommandHandler f = findHandler(handlers, cmd);
        if (!f.deadAllowed() && !player.isAlive() && World.THIS.getState() == GamePlayerState.PLAYING) {
            return Optional.of("Эта команда сейчас не разрешена, так как вы умерли.");
        }
        if (World.THIS.getState() != GamePlayerState.PLAYING && !f.pregameAllowed()) {
            return Optional.of("Эта команда не разрешена вне игры.");
        }
        return f.exec(player, args.stream().skip(1));
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
        registerHandler(new WhisperCommand());
        registerHandler(new YellCommand());
        registerHandler(new SpendPointsCommand());
        registerHandler(new ManCommand(handlers));
    }

    private void registerHandler(ICommandHandler handler) {
        handler.getCommand().forEach(c -> handlers.put(c, handler));
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(.+)", Pattern.DOTALL);
    }

}
