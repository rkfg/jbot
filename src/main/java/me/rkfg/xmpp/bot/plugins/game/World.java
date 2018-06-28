package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.game.effect.BattleFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.NoGuardSleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect.SleepType;
import me.rkfg.xmpp.bot.plugins.game.effect.StaminaRegenEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;

public class World extends Player {

    public static final World THIS = new World();
    private Map<String, IPlayer> players = new HashMap<>();

    private enum GameState {
        GATHER, PLAYING, FINISHED
    }

    private GameState state = GameState.GATHER;

    public World() {
        super("ZAWARUDO");
    }

    public void init() {
        new Timer("Game clock", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (World.this) {
                    players.values().forEach(p -> p.enqueueEvent(new TickEvent()));
                }
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
    }

    public IPlayer getCurrentPlayer(Message message) {
        final IPlayer player = players.computeIfAbsent(message.getFrom(), Player::new);
        initPlayer(player);
        return player;
    }

    public List<IPlayer> listPlayers() {
        return players.values().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
    }

    private void initPlayer(IPlayer player) {
        if (state == GameState.GATHER && player.listEffects().isEmpty()) {
            player.enqueueEvent(new RenameEvent(this, "Test name"));
            player.enqueueEvents(new SetSleepEvent(SleepType.DEEP, this));
            player.enqueueAttachEffect(new BattleFatigueEffect(this, 5));
            player.enqueueAttachEffect(new StaminaRegenEffect(this));
            StatsEffect statsEffectFat = new StatsEffect("fat", "жиробасина", this);
            statsEffectFat.setStatChange(ATK, 1);
            statsEffectFat.setStatChange(DEF, -1);
            StatsEffect statsEffectAlco = new StatsEffect("alcoholic", "алкашня", this);
            statsEffectAlco.setStatChange(DEF, -1);
            statsEffectAlco.setStatChange(PRT, -1);
            statsEffectAlco.addEffect(new NoGuardSleepEffect(World.THIS));
            player.enqueueAttachEffect(statsEffectFat);
            player.enqueueAttachEffect(statsEffectAlco);
        }
    }

    public void announce(String message) {
        players.values().forEach(p -> p.log("=== ОБЪЯВЛЕНИЕ: " + message + " ==="));
    }

}
