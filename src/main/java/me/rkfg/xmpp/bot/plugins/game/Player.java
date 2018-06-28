package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.effect.DeadEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class Player extends AbstractEffectReceiver implements IMutablePlayer {

    private class LogEntry {
        String message;
        long timestamp;

        public LogEntry(String message) {
            this.message = message;
            timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%1$td-%<tm %<tT] %2$s", new Date(timestamp), message);
        }
    }

    private List<LogEntry> log = new LinkedList<>();

    private TypedAttributeMap stats = new TypedAttributeMap();
    private String id;
    private String name;

    private BinaryOperator<String> pipeReducer = (acc, v) -> acc + " | " + v;

    public Player(String id) {
        this.id = id;
        stats.put(HP, 10);
        stats.put(ATK, 10);
        stats.put(DEF, 10);
        stats.put(STR, 5);
        stats.put(PRT, 5);
        stats.put(LCK, 10);
        stats.put(STM, 10);
    }

    @Override
    public boolean isAlive() {
        return !hasEffect(DeadEffect.TYPE);
    }

    @Override
    public Integer getStat(TypedAttribute<Integer> attr) {
        return stats.get(attr).orElse(0);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void changeStat(TypedAttribute<Integer> attr, Integer diff) {
        stats.get(attr).ifPresent(s -> stats.put(attr, Math.max(s + diff, 0))); // clamped to zero
    }

    @Override
    public void dumpStats() {
        StringBuilder sb = new StringBuilder("Статы: ");
        sb.append(STATS.stream().map(attr -> stats.get(attr).map(stat -> attr.getName() + ": " + stat)).filter(Optional::isPresent)
                .map(Optional::get).reduce(pipeReducer).orElse("нет стат"));
        sb.append("\nЭффекты: ")
                .append(listEffects().stream().map(effect -> effect.getType() + String.format(" [%s]", effect.getDescription()))
                        .reduce(pipeReducer).orElse("нет эффектов"));
        log(sb.toString());
    }

    @Override
    public String getLog() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry logEntry : log) {
            sb.append(logEntry.toString()).append("\n");
        }
        log.clear();
        return sb.toString();
    }

    @Override
    public void log(String message) {
        log.add(new LogEntry(message));
    }

    @Override
    public void setDead(boolean dead) {
        final boolean alreadyDead = hasEffect(DeadEffect.TYPE);
        if (alreadyDead != dead) {
            if (dead) {
                enqueueAttachEffect(new DeadEffect());
            } else {
                enqueueDetachEffect(DeadEffect.TYPE, World.THIS);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == PLAYER_OBJ || type == MUTABLEPLAYER_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }
}
