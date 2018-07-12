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
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.repository.ArmorRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.EffectRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.MessageRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.NameRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.UsableRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.WeaponRepository;

public class World extends Player {

    public static final World THIS = new World();
    private Map<String, IPlayer> players = new HashMap<>();
    private NameRepository nameRepository;

    private List<String> names;
    private MessageRepository messageRepository;
    private WeaponRepository weaponRepository;
    private ArmorRepository armorRepository;
    private EffectRepository effectRepository;
    private UsableRepository usableRepository;
    private Timer timer;

    public World() {
        super("ZAWARUDO");
        setState(GamePlayerState.GATHER);
    }

    public void init() {
        nameRepository = new NameRepository();
        nameRepository.loadContent();
        names = nameRepository.getAllContent().stream().map(tm -> tm.get(DESC_CNT)).map(Optional::get).collect(Collectors.toList());
        messageRepository = new MessageRepository();
        messageRepository.loadContent();
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
        timer = new Timer("Game clock", true);
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

    private void generateTraits(IPlayer player) {
        player.as(MUTABLEPLAYER_OBJ).ifPresent(IMutablePlayer::reset);
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
            setState(GamePlayerState.GATHER);
        }
        if (alive.size() == 1) {
            final IPlayer winner = alive.get(0);
            announce("Игра завершена, последний выживший — " + winner.getName() + " aka " + winner.getId());
            winner.log("Вы победили!");
            setState(GamePlayerState.GATHER);
        }
        if (getState() == GamePlayerState.GATHER) {
            stopTime();
            players.values().stream().map(p -> p.as(MUTABLEPLAYER_OBJ)).filter(Optional::isPresent).map(Optional::get)
                    .forEach(p -> p.setState(GamePlayerState.NONE));
        }
    }

    public NameRepository getNameRepository() {
        return nameRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
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
        switch (getState()) {
        case GATHER:
            switch (player.getState()) {
            case NONE:
                player.log("Чтобы вступить в игру, напишите %гм участвую");
                break;
            case READY:
                player.log("Вы готовы начать игру.");
                break;
            case GATHER:
                player.log("Чтобы подтвердить свою готовность, напишите %гм готов");
                break;
            default:
                player.log("Неверный статус.");
                break;
            }
            break;
        case PLAYING:
            player.dumpStats();
            break;
        default:
            break;
        }
    }

    public Optional<String> setPlayerState(IPlayer player, GamePlayerState playerState) {
        if (getState() == GamePlayerState.GATHER) {
            player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
                p.setState(playerState);
                switch (playerState) {
                case NONE:
                    player.log("Вы не будете участвовать в игре.");
                    break;
                case READY:
                    announce(String.format("Игрок %s готов начать игру.", p.getId()));
                    int readyCnt = 0;
                    int gatherCnt = 0;
                    for (IPlayer p2 : players.values()) {
                        if (p2.getState() == GamePlayerState.READY) {
                            ++readyCnt;
                        }
                        if (p2.getState() != GamePlayerState.NONE) {
                            ++gatherCnt;
                        }
                    }
                    if (gatherCnt == 0) {
                        gatherCnt = 1; // should never happen
                    }
                    long readyPlayersPct = readyCnt * 100 / gatherCnt;
                    if (readyPlayersPct >= 75 && gatherCnt > 1) {
                        startGame();
                    } else {
                        player.log("Чтобы отменить свою готовность, напишите %гм готов 0");
                    }
                    break;
                case GATHER:
                    player.log("Вы будете участвовать в игре. Чтобы отказаться от участия, напишите %гм участвую 0");
                    break;
                default:
                    player.log("Неверный статус.");
                    break;
                }
            });
        } else if (getState() == GamePlayerState.PLAYING)

        {
            if (player.getState() != GamePlayerState.PLAYING) {
                return Optional.of("Игра уже идёт, дождитесь следующего раунда.");
            } else {
                return Optional.of("Игра уже идёт, и вы участвуете.");
            }
        }
        return Optional.empty();
    }

    private void startGame() {
        setState(GamePlayerState.PLAYING);
        players.entrySet().removeIf(e -> {
            GamePlayerState r = e.getValue().getState();
            return r != GamePlayerState.READY && r != GamePlayerState.GATHER;
        }); // remove non-participating players
        players.keySet().stream().flatMap(u -> Main.INSTANCE.getRoomsWithUser(u).stream()).distinct().filter(Main.INSTANCE::isDirectChat)
                .forEach(roomId -> Main.INSTANCE.sendMessage("Игра начинается!", roomId));
        initPlayers();
        startTime();
    }

    private void initPlayers() {
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
