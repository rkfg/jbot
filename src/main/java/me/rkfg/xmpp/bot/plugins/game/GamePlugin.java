package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
import me.rkfg.xmpp.bot.plugins.game.effect.BleedEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.NoGuardSleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class GamePlugin extends CommandPlugin {

    private Map<String, Player> players = new HashMap<>();

    private enum GameState {
        GATHER, PLAYING, FINISHED
    }

    private GameState state = GameState.GATHER;
    private static final Optional<String> NORESULT = Optional.empty();
    private static final Optional<String> SLEEP_HELP = Optional
            .of("Неверный режим сна. Укажите значение цифрой: 0 — глубокий сон, 1 — сон вполглаза, 2 — бодрствование.");

    @Override
    public void init() {
        new Timer("Game clock", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (GamePlugin.this) {
                    players.values().forEach(p -> p.enqueueEvent(new TickEvent()));
                }
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
    }

    private Map<String, Function<Stream<String>, Optional<String>>> handlers = new HashMap<>();

    @Override
    public synchronized String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        List<String> args = new ArrayList<>();
        final String argsStr = matcher.group(COMMAND_GROUP);
        if (argsStr != null) {
            args = Stream.of(argsStr.split(" ")).filter(c -> !c.isEmpty()).collect(Collectors.toList());
        }
        IPlayer player = getCurrentPlayer(message);
        handlers.put("", a -> {
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
            return Optional.empty();
        });
        if (!player.isAlive()) {
            player.dumpStats();
            player.log("Вы умерли и не можете играть далее.");
        } else {
            initPlayer(player);
            String cmd = args.stream().findFirst().orElse("");
            return handlers.get(cmd).apply(args.stream().skip(1)).orElseGet(player::getLog);
        }
        return player.getLog();
    }

    public void initPlayer(IPlayer player) {
        if (state == GameState.GATHER && player.listEffects().isEmpty()) {
            ((IMutablePlayer) player).setName("Test name");
            player.enqueueEvent(new SetSleepEvent(SleepType.DEEP, IPlayer.WORLD));
            StatsEffect statsEffectFat = new StatsEffect("fat", "жиробасина", IPlayer.WORLD);
            statsEffectFat.setStatChange(Player.ATK, 1);
            statsEffectFat.setStatChange(Player.DEF, -1);
            StatsEffect statsEffectAlco = new StatsEffect("alcoholic", "алкашня", IPlayer.WORLD);
            statsEffectAlco.setStatChange(Player.DEF, -1);
            statsEffectAlco.setStatChange(Player.PRT, -1);
            statsEffectAlco.addEffect(new NoGuardSleepEffect(IPlayer.WORLD));
            player.enqueueEvents(new EffectEvent(statsEffectFat), new EffectEvent(statsEffectAlco));
            player.enqueueEvent(new EffectEvent(new BleedEffect(IPlayer.WORLD, 2)));
        }
    }

    public IPlayer getCurrentPlayer(Message message) {
        return players.computeIfAbsent(message.getFrom(), Player::new);
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("gm", "гм");
    }

}
