package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.message.BotMessage;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.repository.ArmorRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.EffectRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.MessageRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.NameRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.TraitsRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.UsableRepository;
import me.rkfg.xmpp.bot.plugins.game.repository.WeaponRepository;

public class World extends Player {

    public static final int TICKRATE = 15;
    public static final World THIS = new World();
    private Map<String, IPlayer> players = new HashMap<>();
    private NameRepository nameRepository;

    private List<String> names;
    private MessageRepository messageRepository;
    private WeaponRepository weaponRepository;
    private ArmorRepository armorRepository;
    private EffectRepository effectRepository;
    private UsableRepository usableRepository;
    private TraitsRepository traitsRepository;
    private Timer timer;

    public World() {
        super("ZAWARUDO");
        setState(GamePlayerState.GATHER);
    }

    public void init(String dataDir) {
        nameRepository = new NameRepository(dataDir);
        nameRepository.loadContent();
        names = nameRepository.getAllContent().stream().map(tm -> tm.get(DESC_CNT)).map(Optional::get).collect(Collectors.toList());
        messageRepository = new MessageRepository(dataDir);
        messageRepository.loadContent();
        effectRepository = new EffectRepository(dataDir);
        effectRepository.loadContent();
        weaponRepository = new WeaponRepository(dataDir);
        weaponRepository.loadContent();
        armorRepository = new ArmorRepository(dataDir);
        armorRepository.loadContent();
        usableRepository = new UsableRepository(dataDir);
        usableRepository.loadContent();
        traitsRepository = new TraitsRepository(dataDir);
        traitsRepository.loadContent();
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
        }, TimeUnit.SECONDS.toMillis(TICKRATE), TimeUnit.SECONDS.toMillis(TICKRATE));
    }

    public void stopTime() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public Optional<IPlayer> getCurrentPlayer(BotMessage message) {
        final Optional<IPlayer> player = Optional.ofNullable(players.computeIfAbsent(message.getFrom(), id -> {
            if (getState() == GamePlayerState.PLAYING) {
                return null;
            }
            return new Player(id);
        }));
        player.flatMap(p -> p.as(MUTABLEPLAYER_OBJ)).ifPresent(p -> {
            p.setRoomId(message.getFromRoom());
        });
        return player;
    }

    public List<IPlayer> listPlayers() {
        Comparator<? super IPlayer> comparator = (p1, p2) -> p1.getName().compareTo(p2.getName());
        if (getState() != GamePlayerState.PLAYING) {
            comparator = (p1, p2) -> p1.getId().compareTo(p2.getId());
        }
        return players.values().stream().sorted(comparator).collect(Collectors.toList());
    }

    public List<IPlayer> listAlivePlayers() {
        return players.values().stream().filter(p -> p.isAlive()).collect(Collectors.toList());
    }

    private void generateTraits(IPlayer player) {
        player.as(MUTABLEPLAYER_OBJ).ifPresent(IMutablePlayer::reset);
        Stream.of("const", "mental", "addict").forEach(s -> {
            if (dice("1d6") < 5) {
                traitsRepository.getRandomObject(GROUP_IDX, s).ifPresent(player::enqueueAttachEffect);
            }
        });
    }

    public void announce(String message) {
        players.values().stream().filter(p -> p.getState() != GamePlayerState.NONE).forEach(p -> {
            p.log("<_dgray><yellow>=== " + message + " ===</yellow></_dgray>");
            p.flushLogs();
        });
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
            players.values().stream().map(p -> p.as(MUTABLEPLAYER_OBJ)).forEach(p -> p.ifPresent(pp -> pp.setState(GamePlayerState.NONE)));
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

    public TraitsRepository getTraitsRepository() {
        return traitsRepository;
    }

    public void defaultCommand(IPlayer player) {
        switch (getState()) {
        case GATHER:
            switch (player.getState()) {
            case NONE:
                player.log("Чтобы вступить в игру, напишите " + GamePlugin.CMD + "участвую");
                break;
            case READY:
                player.log("Вы готовы начать игру.");
                break;
            case GATHER:
                player.log("Чтобы подтвердить свою готовность, напишите " + GamePlugin.CMD + "готов");
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
                        player.log("Чтобы отменить свою готовность, напишите " + GamePlugin.CMD + "готов 0");
                    }
                    break;
                case GATHER:
                    player.log("Вы будете участвовать в игре. Чтобы отказаться от участия, напишите " + GamePlugin.CMD + "участвую 0");
                    break;
                default:
                    player.log("Неверный статус.");
                    break;
                }
            });
        } else if (getState() == GamePlayerState.PLAYING) {
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
        players.values().stream().forEach(p -> p.log("Игра начинается!"));
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
            player.getDescription().ifPresent(player::log);
            player.dumpStats();
            pIdx++;
        }
    }

    @Override
    public void reset() {
        resetEffects();
        players.clear();
    }

    @Override
    public void flushLogs() {
        players.values().stream().forEach(IPlayer::flushLogs);
    }

    public void broadcastEvent(IPlayer excludePlayer, Function<IPlayer, IEvent> eventFunction) {
        broadcastEvent(p -> p != excludePlayer, eventFunction);
    }

    public void broadcastEvent(Predicate<IPlayer> filter, Function<IPlayer, IEvent> eventFunction) {
        players.values().stream().filter(filter).forEach(p -> p.enqueueEvent(eventFunction.apply(p)));
    }

}
