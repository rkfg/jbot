package me.rkfg.xmpp.bot.plugins.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.CommandPlugin;
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

    @Override
    public void init() {
        new Timer("Game clock", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (GamePlugin.this) {
                    for (Player player : players.values()) {
                        player.enqueueEvent(new TickEvent());
                    }
                }
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
    }

    @Override
    public synchronized String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        List<String> args = new ArrayList<>();
        final String argsStr = matcher.group(COMMAND_GROUP);
        if (argsStr != null) {
            args = Stream.of(argsStr.split(" ")).filter(c -> !c.isEmpty()).collect(Collectors.toList());
        }
        IPlayer player = getCurrentPlayer(message);
        if (state == GameState.GATHER && player.listEffects().isEmpty()) {
            ((Player) player).setName("Test name");
            StatsEffect statsEffect = new StatsEffect("fat", "жиробасина", Player.WORLD);
            statsEffect.setStatChange(Player.ATK, 1);
            statsEffect.setStatChange(Player.DEF, -1);
            StatsEffect statsEffectAlco = new StatsEffect("alcoholic", "алкашня", Player.WORLD);
            statsEffectAlco.setStatChange(Player.DEF, -1);
            statsEffectAlco.setStatChange(Player.PRT, -1);
            statsEffectAlco.addEffect(new NoGuardSleepEffect(Player.WORLD));
            player.enqueueEvents(new EffectEvent(statsEffect), new EffectEvent(statsEffectAlco),
                    new SetSleepEvent(SleepType.DEEP, Player.WORLD));
        }
        if (args.isEmpty()) {
            player.dumpStats();
        } else {
            String cmd = args.get(0);
            if ("спать".equals(cmd)) {
                if (args.size() == 1) {
                    player.findEffect(SleepEffect.SLEEP_EFFECT).ifPresent(e -> player.log(
                            "Выбранный режим сна: " + e.getAttribute(SleepEffect.SLEEP_TYPE_ATTR).map(SleepType::getLocalized).orElse("")));
                }
                if (args.size() == 2) {
                    try {
                        Integer sleepInt = Integer.valueOf(args.get(1));
                        if (sleepInt < 0 || sleepInt > 2) {
                            return "неверный режим сна";
                        }
                        player.enqueueEvent(new SetSleepEvent(SleepType.values()[sleepInt], player));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return player.getLog();
    }

    public IPlayer getCurrentPlayer(Message message) {
        return players.computeIfAbsent(message.getFrom(), Player::new);
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("gm", "гм");
    }

}
