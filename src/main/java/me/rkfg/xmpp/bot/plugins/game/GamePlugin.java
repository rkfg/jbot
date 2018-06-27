package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class GamePlugin extends CommandPlugin {

    private static final String COMMAND_HELP = "Доступные команды: игроки, спать";
    private static final String COMMAND_HELP_DEAD = "Доступные команды: игроки";

    private static final Optional<String> NORESULT = Optional.empty();
    private static final Optional<String> SLEEP_HELP = Optional
            .of("Неверный режим сна. Укажите значение цифрой: 0 — глубокий сон, 1 — сон вполглаза, 2 — бодрствование.");
    private static final String DEAD_MESSAGE = "Вы умерли и не можете играть далее.";
    private static final Optional<String> ATTACK_HELP = Optional
            .of("Укажите номер атакуемого противника из списка игроков. Список можно посмотреть командой %гм игроки");

    @Override
    public void init() {
        World.THIS.init();
    }

    private Map<String, Function<Stream<String>, Optional<String>>> handlers = new HashMap<>();
    private Map<String, Function<Stream<String>, Optional<String>>> deadHandlers = new HashMap<>();

    @Override
    public synchronized String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        List<String> args = new ArrayList<>();
        final String argsStr = matcher.group(COMMAND_GROUP);
        if (argsStr != null) {
            args = Stream.of(argsStr.split(" ")).filter(c -> !c.isEmpty()).collect(Collectors.toList());
        }
        IPlayer player = World.THIS.getCurrentPlayer(message);
        setupHandlers(player);
        if (!player.isAlive()) {
            return processCommand(deadHandlers, args, player).map(m -> m + "\n" + DEAD_MESSAGE).orElse(COMMAND_HELP_DEAD);
        }
        return processCommand(handlers, args, player).orElse(processCommand(deadHandlers, args, player).orElse(COMMAND_HELP));
    }

    public Optional<String> processCommand(Map<String, Function<Stream<String>, Optional<String>>> handlerMap, List<String> args,
            IPlayer player) {
        String cmd = args.stream().findFirst().map(String::toLowerCase).orElse("");
        Function<Stream<String>, Optional<String>> f = handlerMap.get(cmd);
        if (f == null) {
            return Optional.empty();
        }
        return Optional.of(f.apply(args.stream().skip(1)).orElseGet(player::getLog));
    }

    public void setupHandlers(IPlayer player) {
        deadHandlers.put("", a -> {
            player.dumpStats();
            return NORESULT;
        });
        handlers.put("спать", a -> {
            Optional<String> arg = a.findFirst();
            if (arg.isPresent()) {
                try {
                    SleepType sleepType = arg.map(Integer::valueOf).filter(v -> v >= 0 && v < 3).map(v -> SleepType.values()[v])
                            .orElseThrow(NumberFormatException::new);
                    player.enqueueEvent(new SetSleepEvent(sleepType, player));
                } catch (NumberFormatException e) {
                    return SLEEP_HELP;
                }
            } else {
                return player.findEffect(SleepEffect.TYPE).map(
                        e -> "Выбранный режим сна: " + e.getAttribute(SleepEffect.SLEEP_TYPE_ATTR).map(SleepType::getLocalized).orElse(""));
            }
            return NORESULT;
        });
        deadHandlers.put("игроки", a -> {
            List<IPlayer> playersList = World.THIS.listPlayers();
            if (playersList.isEmpty()) {
                return Optional.of("Игроков нет.");
            }
            return IntStream.range(0, playersList.size()).mapToObj(i -> {
                final IPlayer p = playersList.get(i);
                return "" + (i + 1) + ": " + p.getName() + (p.isAlive() ? "" : " [мёртв]");
            }).reduce((acc, p) -> acc + ", " + p).map(list -> "Игроки: " + list);
        });
        handlers.put("атака", a -> {
            try {
                List<IPlayer> playersList = World.THIS.listPlayers();
                IPlayer target = a.findFirst().map(Integer::valueOf).filter(v -> v > 0 && v <= playersList.size())
                        .map(i -> playersList.get(i - 1)).orElseThrow(NumberFormatException::new);
                if (target == player) {
                    return Optional.of("нельзя атаковать себя");
                }
                if (!player.enqueueEvent(new BattleBeginsEvent(player)) || !target.enqueueEvent(new BattleBeginsEvent(player))) {
                    return Optional.of("Не удалось начать бой");
                }
            } catch (NumberFormatException e) {
                return ATTACK_HELP;
            }
            return NORESULT;
        });
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("gm", "гм");
    }

}
