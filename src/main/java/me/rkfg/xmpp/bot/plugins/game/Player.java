package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.Main;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.BattleFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.DeadEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.EquipRedirectorEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.ExpiringBonusPointsEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.LootEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SearchFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SpeechFatigueEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StaminaRegenEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.trait.BattleAuraEffect;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
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

    public static final int BASE_BONUS_POINTS = 5;

    public static final int BASE_LCK = 10;

    public static final int BASE_PRT = 5;

    public static final int BASE_STR = 5;

    public static final int BASE_DEF = 10;

    public static final int BASE_ATK = 10;

    public static final int BASE_HP = 30;

    public static final int BONUS_EXPIRATON_TICKS = 8;

    public static final int BASE_STM = 15;

    private static final String UNNAMED = "<безымянный>";

    public static final int BATTLE_FATIGUE_COST = 5;

    public static final int AMBUSH_FATIGUE_COST = 5;

    public static final int HIDE_FATIGUE_COST = 2;

    public static final int SEARCH_FATIGUE_COST = 4;

    public static final int YELL_FATIGUE_COST = 2;

    public static final int WHISPER_FATIGUE_COST = 1;

    private class LogEntry {
        String message;

        public LogEntry(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private List<LogEntry> log = new LinkedList<>();

    private TypedAttributeMap stats = new TypedAttributeMap();
    private TypedAttributeMap equipment = new TypedAttributeMap();
    private String id;
    private String roomId = "";
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
        stats.get(BONUS_POINTS).filter(p -> p > 0).ifPresent(p -> sb.append(" | Бонусные очки: ").append(p));
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
    public void flushLogs() {
        final String logStr = getLog();
        if (!logStr.isEmpty()) {
            Main.INSTANCE.sendMessage(logStr, roomId);
        }
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
    public TypedAttributeMap getAttrs() {
        return stats;
    }

    @Override
    public void reset() {
        resetEffects();
        stats = new TypedAttributeMap();
        equipment = new TypedAttributeMap();
        backpack = new ArrayList<>();
        name = UNNAMED;
        stats.put(HP, BASE_HP);
        stats.put(ATK, BASE_ATK);
        stats.put(DEF, BASE_DEF);
        stats.put(STR, BASE_STR);
        stats.put(PRT, BASE_PRT);
        stats.put(LCK, BASE_LCK);
        stats.put(STM, BASE_STM);
        stats.put(BONUS_POINTS, BASE_BONUS_POINTS);
        equipment.put(WEAPON_SLOT, new Slot("держит в руках"));
        equipment.put(ARMOR_SLOT, new Slot("одет в"));
        enqueueAttachEffect(new BattleFatigueEffect(BATTLE_FATIGUE_COST));
        enqueueAttachEffect(new HideFatigueEffect(HIDE_FATIGUE_COST));
        enqueueAttachEffect(new SearchFatigueEffect(SEARCH_FATIGUE_COST));
        enqueueAttachEffect(new AmbushFatigueEffect(AMBUSH_FATIGUE_COST));
        enqueueAttachEffect(new StaminaRegenEffect());
        enqueueAttachEffect(new EquipRedirectorEffect());
        enqueueAttachEffect(new LootEffect());
        enqueueAttachEffect(new BattleAuraEffect());
        enqueueAttachEffect(new SpeechFatigueEffect(YELL_FATIGUE_COST, WHISPER_FATIGUE_COST));
        enqueueAttachEffect(new ExpiringBonusPointsEffect(5, BONUS_EXPIRATON_TICKS));
        setState(GamePlayerState.PLAYING);
    }

    @Override
    public void setState(GamePlayerState state) {
        setAttribute(READY, state);
    }

    @Override
    public GamePlayerState getState() {
        return getAttribute(READY).orElse(GamePlayerState.NONE);
    }

    @Override
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    @Override
    public Optional<String> getDescription() {
        Optional<String> description = listEffects().stream().map(e -> capitalize(e.getDescription(Verbosity.VERBOSE).orElse("")))
                .filter(s -> !s.isEmpty()).reduce((a, s) -> {
                    if (a.endsWith(".")) {
                        return a + " " + s;
                    }
                    return a + ". " + s;
                });
        if (!description.isPresent()) {
            return Optional.of("Об этом персонаже нельзя сказать ничего особенного.");
        }
        return description;
    }
}