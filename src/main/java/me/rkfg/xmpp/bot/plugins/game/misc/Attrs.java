package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.repository.AbstractContentRepository.IndexPointer;

public class Attrs {
    public enum GamePlayerState {
        NONE, GATHER, READY, PLAYING
    }

    private Attrs() {
    }

    public static final TypedAttribute<Integer> HP = TypedAttribute.of("Здоровье");
    public static final TypedAttribute<Integer> STM = TypedAttribute.of("Энергия");
    public static final TypedAttribute<Integer> LCK = TypedAttribute.of("Удача");
    public static final TypedAttribute<Integer> PRT = TypedAttribute.of("Броня");
    public static final TypedAttribute<Integer> STR = TypedAttribute.of("Сила");
    public static final TypedAttribute<Integer> DEF = TypedAttribute.of("Защита");
    public static final TypedAttribute<Integer> ATK = TypedAttribute.of("Атака");
    public static final List<TypedAttribute<Integer>> STATS = Arrays.asList(HP, STM, ATK, DEF, STR, PRT, LCK);

    public static final TypedAttribute<ISlot> WEAPON_SLOT = TypedAttribute.of("оружие");
    public static final TypedAttribute<ISlot> ARMOR_SLOT = TypedAttribute.of("броня", "броню");
    public static final TypedAttribute<ISlot> ITEM_SLOT = TypedAttribute.of("предмет");
    public static final List<TypedAttribute<ISlot>> SLOTS = Arrays.asList(WEAPON_SLOT, ARMOR_SLOT);
    public static final TypedAttribute<List<IItem>> BACKPACK = TypedAttribute.of("рюкзак");

    // --- use in IGameObject::as to optionally downcast ---
    public static final TypedAttribute<IPlayer> PLAYER_OBJ = TypedAttribute.of("playerobj");
    public static final TypedAttribute<IMutablePlayer> MUTABLEPLAYER_OBJ = TypedAttribute.of("mutableplayerobj");
    public static final TypedAttribute<IHasStats> STATS_OBJ = TypedAttribute.of("statsobj");
    public static final TypedAttribute<IMutableStats> MUTABLESTATS_OBJ = TypedAttribute.of("mutablestatsobj");
    public static final TypedAttribute<IItem> ITEM_OBJ = TypedAttribute.of("itemobj");
    public static final TypedAttribute<IWeapon> WEAPON_OBJ = TypedAttribute.of("weaponobj");
    public static final TypedAttribute<IArmor> ARMOR_OBJ = TypedAttribute.of("armorobj");
    // --- use in IGameObject::as to optionally downcast ---

    public static final TypedAttribute<String> CONTENT_ID = TypedAttribute.of("contentid");
    public static final TypedAttribute<Integer> LIFETIME = TypedAttribute.of("lifetime");
    public static final TypedAttribute<Integer> FATIGUE = TypedAttribute.of("fatigue");
    public static final TypedAttribute<Set<TypedAttributeMap>> EFFECTS = TypedAttribute.of("effects");

    public static final TypedAttribute<Integer> TIER_CNT = TypedAttribute.of("tiercnt");
    public static final IndexPointer<Integer> TIER_IDX = IndexPointer.named("tieridx");

    public static final TypedAttribute<String> DESC_CNT = TypedAttribute.of("desccnt");
    public static final IndexPointer<String> DESC_IDX = IndexPointer.named("descidx");

    public static final TypedAttribute<String> GROUP_ID = TypedAttribute.of("groupid");
    public static final IndexPointer<String> GROUP_IDX = IndexPointer.named("groupidx");

    public static final TypedAttribute<String> DESC_V_CNT = TypedAttribute.of("descvcnt");

    public static final TypedAttribute<List<String>> EFFECT_PARAMS = TypedAttribute.of("effectparams");
    public static final TypedAttribute<Map<String, String>> EFFECT_PARAMS_KV = TypedAttribute.of("effectparamskv");

    public static final TypedAttribute<Integer> USE_CNT = TypedAttribute.of("использования");
    public static final TypedAttribute<String> DESCRIPTION = TypedAttribute.of("description");
    public static final TypedAttribute<String> DESCRIPTION_V = TypedAttribute.of("verbosedescription");
    public static final TypedAttribute<String> OBJTYPE = TypedAttribute.of("objtype");
    public static final TypedAttribute<String> OTHERTYPE = TypedAttribute.of("othertype");
    public static final TypedAttribute<IItem> ITEM = TypedAttribute.of("item");
    public static final TypedAttribute<Boolean> SETEFFECTACTIVE = TypedAttribute.of("seteffectactive");
    public static final TypedAttribute<GamePlayerState> READY = TypedAttribute.of("ready");
    public static final TypedAttribute<Set<String>> TRAITS = TypedAttribute.of("traits");
}
