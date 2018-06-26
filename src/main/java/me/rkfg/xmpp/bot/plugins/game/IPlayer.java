package me.rkfg.xmpp.bot.plugins.game;

import java.util.Arrays;
import java.util.List;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IPlayer extends IGameObject {

    public static final TypedAttribute<Integer> HP = TypedAttribute.of("Hitpoints");
    public static final TypedAttribute<Integer> STM = TypedAttribute.of("Stamina");
    public static final TypedAttribute<Integer> LCK = TypedAttribute.of("Luck");
    public static final TypedAttribute<Integer> PRT = TypedAttribute.of("Protection");
    public static final TypedAttribute<Integer> STR = TypedAttribute.of("Strength");
    public static final TypedAttribute<Integer> DEF = TypedAttribute.of("Defense");
    public static final TypedAttribute<Integer> ATK = TypedAttribute.of("Attack");
    public static final List<TypedAttribute<Integer>> STATS = Arrays.asList(HP, STM, ATK, DEF, STR, PRT, LCK);

    public static final Player WORLD = new Player("ZAWARUDO"); // dummy object for placeholder and log purposes

    boolean isAlive();

    String getId();

    String getName();

    void dumpStats();

    String getLog();

    Integer getStat(TypedAttribute<Integer> attr);

}
