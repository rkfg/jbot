package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.ItemPickupEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SetSleepEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.repository.ArmorRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.EffectRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.NameRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.WeaponRepository;

public class World extends Player {

    public static final World THIS = new World();
    private Map<String, IPlayer> players = new HashMap<>();
    private NameRepository nameRepository;

    private enum GameState {
        GATHER, PLAYING, FINISHED
    }

    private GameState state = GameState.GATHER;
    private List<String> names;
    private WeaponRepository weaponRepository;
    private ArmorRepository armorRepository;
    private EffectRepository effectRepository;

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
        nameRepository = new NameRepository();
        nameRepository.loadContent();
        names = nameRepository.getAllContent().stream().map(tm -> tm.get(NameRepository.DESC_CNT)).map(Optional::get)
                .collect(Collectors.toList());
        effectRepository = new EffectRepository();
        effectRepository.loadContent();
        weaponRepository = new WeaponRepository(effectRepository);
        weaponRepository.loadContent();
        armorRepository = new ArmorRepository();
        armorRepository.loadContent();
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
            if (players.size() % names.size() == 1) {
                Collections.shuffle(names);
            }
            int round = (players.size() - 1) / names.size() + 1;
            String name = names.get(players.size() % names.size());
            if (round > 1) {
                name += " " + round + "-й";
            }
            player.enqueueEvent(new RenameEvent(this, name));
            player.enqueueEvents(new SetSleepEvent(SleepType.DEEP, this));
            effectRepository.getObjectById(BattleFatigueEffect.TYPE).ifPresent(player::enqueueAttachEffect);
            player.enqueueAttachEffect(new StaminaRegenEffect());
            StatsEffect statsEffectFat = new StatsEffect("fat", "жиробасина");
            statsEffectFat.setStatChange(ATK, 1);
            statsEffectFat.setStatChange(DEF, -1);
            StatsEffect statsEffectAlco = new StatsEffect("alcoholic", "алкашня");
            statsEffectAlco.setStatChange(DEF, -1);
            statsEffectAlco.setStatChange(PRT, -1);
            statsEffectAlco.addEffect(new NoGuardSleepEffect());
            player.enqueueAttachEffect(statsEffectFat);
            player.enqueueAttachEffect(statsEffectAlco);
            weaponRepository.getObjectById("pen").ifPresent(w -> player.enqueueEvent(new EquipEvent(w)));
            // weaponRepository.getRandomObjectByTier(1).ifPresent(w -> player.enqueueEvent(new EquipEvent(w)));
            armorRepository.getRandomObjectByTier(1).ifPresent(a -> player.enqueueEvent(new EquipEvent(a)));
        }
    }

    public void announce(String message) {
        players.values().forEach(p -> p.log("=== ОБЪЯВЛЕНИЕ: " + message + " ==="));
    }

    public void checkVictory() {
        List<IPlayer> alive = players.values().stream().filter(IPlayer::isAlive).collect(Collectors.toList());
        if (alive.isEmpty()) {
            announce("Игра завершена, выживших нет.");
            state = GameState.FINISHED;
        }
        if (alive.size() == 1) {
            final IPlayer winner = alive.get(0);
            announce("Игра завершена, последний выживший — " + winner.getName() + " aka " + winner.getId());
            winner.log("Вы победили!");
            state = GameState.FINISHED;
        }
    }
}
