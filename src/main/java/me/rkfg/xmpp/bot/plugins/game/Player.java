package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.effect.DeadEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.ItemPickupEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.exception.NotEquippableException;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.IMutableSlot;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.item.Slot;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.misc.IMutableStats;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class Player extends AbstractEffectReceiver implements IMutablePlayer, IMutableStats {

    private static final String UNNAMED = "<безымянный>";

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
    private TypedAttributeMap equipment = new TypedAttributeMap();
    private String id;
    private String name = UNNAMED;
    private List<IItem> backpack = new ArrayList<>();

    public Player(String id) {
        this.id = id;
        setState(GamePlayerState.NONE);
    }

    @Override
    public boolean isAlive() {
        return !hasEffect(DeadEffect.TYPE);
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
    public void dumpStats() {
        StringBuilder sb = new StringBuilder("Статы: ");
        final String statsStr = STATS.stream().map(attr -> stats.get(attr).map(stat -> {
            Integer modStat = applyWeaponArmorStats(stat, attr);
            return attr.getName() + ": " + (modStat.equals(stat) ? stat : modStat + " (" + stat + ")");
        })).filter(Optional::isPresent).map(Optional::get).reduce(pipeReducer).orElse("нет стат");
        sb.append(statsStr);
        final String effectsStr = listEffects().stream().filter(IEffect::isVisible)
                .map(effect -> effect.getDescription().orElse(effect.getType())).reduce(pipeReducer).orElse("нет эффектов");
        sb.append("\nЭффекты: ").append(effectsStr);
        final String slotsStr = SLOTS.stream()
                .map(slotAttr -> equipment.get(slotAttr)
                        .map(s -> String.format("%s: %s", s.getDescription().orElse(""),
                                s.getItem().flatMap(i -> i.getDescription(Verbosity.WITH_PARAMS)).orElse("пусто")))
                        .orElse(""))
                .reduce(pipeReducer).orElse("нет слотов");
        sb.append("\nСлоты: ").append(slotsStr);
        log(sb.toString());
    }

    private Integer applyWeaponArmorStats(Integer stat, TypedAttribute<Integer> attr) {
        if (attr == ATK) {
            stat += getWeapon().map(IWeapon::getAttack).orElse(0);
        }
        if (attr == DEF) {
            stat += getWeapon().map(IWeapon::getDefence).orElse(0) + getArmor().map(IArmor::getDefence).orElse(0);
        }
        if (attr == STR) {
            stat += getWeapon().map(IWeapon::getStrength).orElse(0);
        }
        if (attr == PRT) {
            stat += getArmor().map(IArmor::getProtection).orElse(0);
        }
        return stat;
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
                enqueueDetachEffect(DeadEffect.TYPE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == PLAYER_OBJ || type == MUTABLEPLAYER_OBJ || type == STATS_OBJ || type == MUTABLESTATS_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ISlot> getSlot(TypedAttribute<ISlot> slot) {
        return equipment.get(slot);
    }

    @Override
    public void equipItem(IItem item) {
        TypedAttribute<ISlot> slotAttr = item.getFittingSlot().orElseThrow(NotEquippableException::new);
        Optional<IMutableSlot> slot = equipment.get(slotAttr).map(s -> (s instanceof IMutableSlot) ? (IMutableSlot) s : null);
        if (!slot.isPresent()) {
            throw new NotEquippableException(String.format("слот [%s] не найден", slotAttr.getName()));
        }
        slot.ifPresent(s -> {
            Optional<IItem> itemInSlot = s.getItem();
            itemInSlot.ifPresent(i -> {
                throw new NotEquippableException(String.format("слот [%s] уже занят предметом [%s]",
                        s.getDescription().orElse("неизвестный"), i.getDescription().orElse("неизвестно")));
            });
            s.setItem(item);
            item.setOwner(this);
        });
    }

    @Override
    public void unequipItem(TypedAttribute<ISlot> slotAttr) {
        equipment.get(slotAttr).map(s -> (s instanceof IMutableSlot) ? (IMutableSlot) s : null).ifPresent(s -> s.setItem(null));
    }

    @Override
    public List<IItem> getBackpack() {
        return Collections.unmodifiableList(backpack);
    }

    @Override
    public void putItemToBackpack(IItem item) {
        backpack.add(item);
        item.setOwner(this);
    }

    @Override
    public void removeFromBackpack(IItem item) {
        backpack.remove(item);
    }

    @Override
    public boolean enqueueEquipItem(IItem item) {
        if (!item.getFittingSlot().map(this::enqueueUnequipItem).orElse(true)) {
            return false;
        }
        final EquipEvent equipEvent = new EquipEvent(item);
        if (enqueueEvent(equipEvent)) {
            equipEvent.equip();
            if (!equipEvent.isCancelled()) {
                removeFromBackpack(item);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean enqueueUnequipItem(TypedAttribute<ISlot> slot) {
        return getSlot(slot).flatMap(ISlot::getItem).map(i -> {
            final UnequipEvent event = new UnequipEvent(slot);
            if (enqueueEvent(event)) {
                event.unequip();
                if (!event.isCancelled()) {
                    putItemToBackpack(i);
                    return true;
                }
            }
            return false;
        }).orElse(true);
    }

    @Override
    public boolean enqueuePickup(IItem item) {
        return enqueueEvent(new ItemPickupEvent(item));
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return stats;
    }

    @Override
    public void reset(boolean init) {
        stats = new TypedAttributeMap();
        equipment = new TypedAttributeMap();
        backpack = new ArrayList<>();
        name = UNNAMED;
        if (init) {
            stats.put(HP, 30);
            stats.put(ATK, 10);
            stats.put(DEF, 10);
            stats.put(STR, 5);
            stats.put(PRT, 5);
            stats.put(LCK, 10);
            stats.put(STM, 10);
            equipment.put(WEAPON_SLOT, new Slot("держит в руках"));
            equipment.put(ARMOR_SLOT, new Slot("одет в"));
            setState(GamePlayerState.PLAYING);
        }
    }

    @Override
    public void setState(GamePlayerState state) {
        setAttribute(READY, state);
    }

    @Override
    public GamePlayerState getState() {
        return getAttribute(READY).orElse(GamePlayerState.NONE);
    }
}