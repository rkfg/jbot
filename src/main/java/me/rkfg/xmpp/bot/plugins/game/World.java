package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.message.Message;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.BattleFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.EquipRedirectorEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SearchFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StaminaRegenEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.repository.ArmorRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.EffectRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.NameRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.UsableRepository;
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
    private UsableRepository usableRepository;
    private Timer timer = new Timer("Game clock", true);

    public World() {
        super("ZAWARUDO");
    }

    public void init() {
        nameRepository = new NameRepository();
        nameRepository.loadContent();
        names = nameRepository.getAllContent().stream().map(tm -> tm.get(DESC_CNT)).map(Optional::get).collect(Collectors.toList());
        effectRepository = new EffectRepository();
        effectRepository.loadContent();
        weaponRepository = new WeaponRepository();
        weaponRepository.loadContent();
        armorRepository = new ArmorRepository();
        armorRepository.loadContent();
        usableRepository = new UsableRepository();
        usableRepository.loadContent();
    }

    public void startTime() {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (World.this) {
                    players.values().forEach(p -> p.enqueueEvent(new TickEvent()));
                }
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
    }

    private void stopTime() {
        timer.cancel();
    }

    public IPlayer getCurrentPlayer(Message message) {
        return players.computeIfAbsent(message.getFrom(), Player::new);
    }

    public List<IPlayer> listPlayers() {
        return players.values().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());
    }

    private void resetPlayer(IPlayer player) {
        if (state == GameState.GATHER) {
            player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
                boolean ready = p.isReady();
                p.reset(false);
                p.setReady(ready);
            });
        }
    }

    public void generateTraits(IPlayer player) {
        player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.reset(true));
        player.enqueueAttachEffect(new BattleFatigueEffect());
        player.enqueueAttachEffect(new HideFatigueEffect());
        player.enqueueAttachEffect(new SearchFatigueEffect());
        player.enqueueAttachEffect(new AmbushFatigueEffect());
        player.enqueueAttachEffect(new StaminaRegenEffect());
        player.enqueueAttachEffect(new EquipRedirectorEffect());
        StatsEffect statsEffectFat = new StatsEffect("fat", "жиробасина");
        statsEffectFat.setStatChange(ATK, 1);
        statsEffectFat.setStatChange(DEF, -1);
        StatsEffect statsEffectAlco = new StatsEffect("alcoholic", "алкашня");
        statsEffectAlco.setStatChange(DEF, -1);
        statsEffectAlco.setStatChange(PRT, -1);
        player.enqueueAttachEffect(statsEffectFat);
        player.enqueueAttachEffect(statsEffectAlco);
        weaponRepository.getObjectById("pen").ifPresent(player::enqueueEquipItem);
        // weaponRepository.getRandomObjectByTier(1).ifPresent(w -> player.enqueueEvent(new EquipEvent(w)));
        armorRepository.getRandomObjectByTier(1).ifPresent(player::enqueueEquipItem);
        weaponRepository.getObjectById("dildo").ifPresent(player::enqueuePickup);
        weaponRepository.getObjectById("lasersaw").ifPresent(player::enqueuePickup);
        usableRepository.getObjectById("bandage").ifPresent(player::enqueuePickup);
        usableRepository.getObjectById("speedhack").ifPresent(player::enqueuePickup);
        usableRepository.getObjectById("energycell").ifPresent(player::enqueuePickup);
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
        if (state == GameState.FINISHED) {
            stopTime();
        }
    }

    public NameRepository getNameRepository() {
        return nameRepository;
    }

    public WeaponRepository getWeaponRepository() {
        return weaponRepository;
    }

    public ArmorRepository getArmorRepository() {
        return armorRepository;
    }

    public EffectRepository getEffectRepository() {
        return effectRepository;
    }

    public UsableRepository getUsableRepository() {
        return usableRepository;
    }

    public void defaultCommand(IPlayer player) {
        switch (state) {
        case GATHER:
            resetPlayer(player);
            player.log("Вы в игре.");
            break;
        case PLAYING:
            player.dumpStats();
            break;
        case FINISHED:
            player.log("Игра завершена.");
            break;
        default:
            break;
        }
    }

    public Optional<String> setPlayerReady(IPlayer player, boolean ready) {
        switch (state) {
        case GATHER:
            player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
                p.setReady(ready);
                announce(String.format("Игрок %s %s начать игру.", p.getId(), ready ? "готов" : "не готов"));
                int readyPlayersPct = players.values().stream().mapToInt(pl -> pl.isReady() ? 1 : 0).sum() * 100 / players.size();
                if (readyPlayersPct >= 75) {
                    state = GameState.PLAYING;
                    players.keySet().stream().flatMap(u -> Main.INSTANCE.getRoomsWithUser(u).stream()).distinct()
                            .filter(Main.INSTANCE::isDirectChat).forEach(roomId -> Main.INSTANCE.sendMessage("Игра начинается!", roomId));
                    initPlayers();
                }
                startTime();
            });
            break;
        case PLAYING:
            return Optional.of("Игра уже идёт, дождитесь следующего раунда.");
        case FINISHED:
            return Optional.of("Игра завершена, дождитесь начала раунда.");
        }
        return Optional.empty();
    }

    public void initPlayers() {
        int pIdx = 0;
        for (Entry<String, IPlayer> entry : players.entrySet()) {
            if (pIdx % names.size() == 0) {
                Collections.shuffle(names);
            }
            int round = pIdx / names.size() + 1;
            String name = names.get(pIdx % names.size());
            if (round > 1) {
                name += " " + round + "-й";
            }
            IPlayer player = entry.getValue();
            generateTraits(player);
            player.enqueueEvent(new RenameEvent(name));
            pIdx++;
        }
    }

}
