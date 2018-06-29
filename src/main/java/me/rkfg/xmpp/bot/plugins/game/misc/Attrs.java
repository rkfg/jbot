package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.Arrays;
import java.util.List;

import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;

public class Attrs {
    private Attrs() {
    }

    public static final TypedAttribute<Integer> HP = TypedAttribute.of("Hitpoints");
    public static final TypedAttribute<Integer> STM = TypedAttribute.of("Stamina");
    public static final TypedAttribute<Integer> LCK = TypedAttribute.of("Luck");
    public static final TypedAttribute<Integer> PRT = TypedAttribute.of("Protection");
    public static final TypedAttribute<Integer> STR = TypedAttribute.of("Strength");
    public static final TypedAttribute<Integer> DEF = TypedAttribute.of("Defense");
    public static final TypedAttribute<Integer> ATK = TypedAttribute.of("Attack");
    public static final List<TypedAttribute<Integer>> STATS = Arrays.asList(HP, STM, ATK, DEF, STR, PRT, LCK);

    public static final TypedAttribute<ISlot> WEAPON_SLOT = TypedAttribute.of("Weapon slot");
    public static final TypedAttribute<ISlot> ARMOR_SLOT = TypedAttribute.of("Armor slot");
    public static final TypedAttribute<List<IItem>> BACKPACK = TypedAttribute.of("Backpack");
    
    // --- use in IGameObject::as to optionally downcast ---
    public static final TypedAttribute<IPlayer> PLAYER_OBJ = TypedAttribute.of("playerobj");
    public static final TypedAttribute<IMutablePlayer> MUTABLEPLAYER_OBJ = TypedAttribute.of("mutableplayerobj");
    public static final TypedAttribute<IItem> ITEM_OBJ = TypedAttribute.of("itemobj");
    // --- use in IGameObject::as to optionally downcast ---
}
