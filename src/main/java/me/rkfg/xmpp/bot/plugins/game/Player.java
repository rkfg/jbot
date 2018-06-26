package me.rkfg.xmpp.bot.plugins.game;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class Player extends AbstractEffectReceiver implements IPlayer {

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

    public static final TypedAttribute<Integer> STM = TypedAttribute.of("Stamina");
    public static final TypedAttribute<Integer> LCK = TypedAttribute.of("Luck");
    public static final TypedAttribute<Integer> PRT = TypedAttribute.of("Protection");
    public static final TypedAttribute<Integer> STR = TypedAttribute.of("Strength");
    public static final TypedAttribute<Integer> DEF = TypedAttribute.of("Defense");
    public static final TypedAttribute<Integer> ATK = TypedAttribute.of("Attack");
    public static final TypedAttribute<Integer> HP = TypedAttribute.of("Hitpoints");

    public static final List<TypedAttribute<Integer>> STATS = Arrays.asList(HP, STM, ATK, DEF, STR, PRT, LCK);

    public static final Player WORLD = new Player("ZAWARUDO") {
    }; // dummy object for placeholder and log purposes

    private TypedAttributeMap stats = new TypedAttributeMap();
    private String id;
    private String name;

    public Player(String id) {
        this.id = id;
        stats.put(HP, 10);
        stats.put(ATK, 10);
        stats.put(DEF, 10);
        stats.put(STR, 10);
        stats.put(PRT, 10);
        stats.put(LCK, 10);
        stats.put(STM, 10);
    }

    @Override
    public boolean isAlive() {
        return stats.get(HP).orElse(0) > 0;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeStat(TypedAttribute<Integer> attr, Integer diff) {
        stats.get(attr).ifPresent(s -> stats.put(attr, Math.max(s + diff, 0))); // clamped to zero
    }

    @Override
    public void dumpStats() {
        StringBuilder sb = new StringBuilder("Статы: ");
        for (TypedAttribute<Integer> attr : STATS) {
            stats.get(attr).ifPresent(stat -> sb.append(attr.getName()).append(": ").append(stat).append(" | "));
        }
        sb.append("\nЭффекты: ");
        listEffects().forEach(effect -> sb.append(effect.getName()).append(String.format(" [%s] | ", effect.getLocalizedName())));
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
}
