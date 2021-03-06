package me.rkfg.xmpp.bot;

import static java.util.Arrays.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.MatrixMessage;
import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.item.ChargeableEffect;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;
import me.rkfg.xmpp.bot.plugins.game.repository.AbstractContentRepository;

public class TestBase {

    protected static final String W_IRONROD = "ironrod";
    protected static final String W_LASERSAW = "lasersaw";
    protected static final String U_ENERGYCELL = "energycell";
    protected static final String U_DOSHIRAKBEEF = "doshirakbeef";
    protected static final String W_CONSOLE = "console";
    protected static final String A_CHEATS = "cheats";
    protected static final String W_GAUNTLET = "gauntlet";
    protected static Logger log = LoggerFactory.getLogger(TestGame.class);
    protected static Random randomMock;
    protected static Random searchRandomMock;

    protected IMutablePlayer player1;
    protected IMutablePlayer player2;
    protected IMutablePlayer player3;
    protected IMutablePlayer player4;
    protected IMutablePlayer player5;

    private static final List<Integer> BASE_STATS = Arrays.asList(Player.BASE_HP, Player.BASE_STM, Player.BASE_ATK, Player.BASE_DEF,
            Player.BASE_STR, Player.BASE_PRT, Player.BASE_LCK);

    @BeforeAll
    static void initWorld() {
        try {
            assertNotNull(World.THIS);
            setStaticField(Main.class, "INSTANCE", mock(IBot.class));
            randomMock = Mockito.mock(Random.class);
            setStaticField(Utils.class, "rnd", randomMock);
            searchRandomMock = Mockito.mock(Random.class);
            setStaticField(AbstractContentRepository.class, "rnd", searchRandomMock);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail(e);
        }
        World.THIS.init("data_test");
    }

    protected static void setStaticField(Class<?> clazz, String fieldname, Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldname);
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
    }

    public void setRandom(Random rnd, Integer... numbers) {
        setRandom(rnd, asList(numbers));
    }

    public void setRandom(Random rnd, List<Integer> numbers) {
        OngoingStubbing<Integer> stubbing = when(rnd.nextInt(anyInt()));
        for (Integer n : numbers) {
            stubbing = stubbing.thenReturn(n);
        }
    }

    public void setDRN(int... numbers) {
        List<Integer> result = new LinkedList<>();
        for (int n : numbers) {
            List<Integer> numResult = new LinkedList<>();
            while (n > 10) {
                numResult.add(5);
                n -= 5;
            }
            int dice1 = Math.floorDiv(n, 2);
            numResult.add(dice1 - 1);
            numResult.add(n - dice1 - 1);
            result.addAll(numResult);
        }
        setRandom(randomMock, result);
    }

    protected IMutablePlayer createPlayer(String id) {
        return World.THIS.getCurrentPlayer(new MatrixMessage(null, null, id, id)).flatMap(p -> p.as(MUTABLEPLAYER_OBJ)).orElse(null);
    }

    protected void equipArmor(IPlayer player, String type) {
        World.THIS.getArmorRepository().getObjectById(type).ifPresent(player::enqueueEquipItem);
    }

    protected int getWeaponCharges(IPlayer player) {
        return player.getWeapon().flatMap(w -> w.getEffect(ChargeableEffect.TYPE)).flatMap(e -> e.getAttribute(ChargeableEffect.CHARGES))
                .orElse(-1);
    }

    protected void flushLogs() {
        player1.getLog();
        player2.getLog();
    }

    protected void dumpLogs() {
        log.info("Player 1 log: {}", player1.getLog());
        log.info("Player 2 log: {}", player2.getLog());
    }

    protected void pickupWeapon(IPlayer player, String type) {
        World.THIS.getWeaponRepository().getObjectById(type).ifPresent(player::enqueuePickup);
    }

    protected void pickupArmor(IPlayer player, String type) {
        World.THIS.getArmorRepository().getObjectById(type).ifPresent(player::enqueuePickup);
    }

    protected void pickupItem(IPlayer player, String type) {
        World.THIS.getUsableRepository().getObjectById(type).ifPresent(player::enqueuePickup);
    }

    protected void equipWeapon(IPlayer player, String type) {
        World.THIS.getWeaponRepository().getObjectById(type).ifPresent(player::enqueueEquipItem);
    }

    protected void applyTrait(IPlayer player, String type) {
        World.THIS.getTraitsRepository().getObjectById(type).ifPresent(player::attachEffect);
    }

    protected void assertBattleSTMChange(IPlayer player, int cnt) {
        assertStatChange(player, STM, -Player.BATTLE_FATIGUE_COST * cnt);
    }

    protected void assertStatChange(IPlayer player, TypedAttribute<Integer> attr, int diff) {
        int i = STATS.indexOf(attr);
        if (i < 0) {
            fail("Stat not found");
        }
        assertEquals(BASE_STATS.get(i) + diff, (int) player.getStat(attr));
    }

    protected void assertHasWeapon(IPlayer player, String type) {
        Optional<String> weaponName = player.getWeapon().map(IWeapon::getType);
        if (!weaponName.isPresent()) {
            fail();
        }
        assertEquals(type, weaponName.get());
    }

    protected void assertHasBackpackItem(IMutablePlayer player, int idx, String type) {
        List<IItem> backpack = player.getBackpack();
        assertTrue(backpack.size() > idx);
        IItem item = backpack.get(idx);
        assertNotNull(item);
        assertEquals(type, item.getType());
    }
}
