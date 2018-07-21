package me.rkfg.xmpp.bot;

import static java.util.Arrays.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.MatrixMessage;
import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.command.EquipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnequipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UseCommand;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.ChargeableEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.RechargeEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class TestGame {

    private IMutablePlayer player1;
    private IMutablePlayer player2;
    private IMutablePlayer player3;
    private IMutablePlayer player4;
    private IMutablePlayer player5;

    private static Logger log = LoggerFactory.getLogger(TestGame.class);
    private static Random randomMock;

    private static void setStaticField(Class<?> clazz, String fieldname, Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldname);
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
    }

    @BeforeAll
    static void initWorld() {
        try {
            assertNotNull(World.THIS);
            setStaticField(Main.class, "INSTANCE", mock(IBot.class));
            randomMock = Mockito.mock(Random.class);
            setStaticField(Utils.class, "rnd", randomMock);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail(e);
        }
        World.THIS.init("data_test");
    }

    public void setRandom(Integer... numbers) {
        setRandom(asList(numbers));
    }

    public void setRandom(List<Integer> numbers) {
        OngoingStubbing<Integer> stubbing = when(randomMock.nextInt(anyInt()));
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
        setRandom(result);
    }

    @BeforeEach
    void init() {
        World.THIS.reset();
        World.THIS.setState(GamePlayerState.GATHER);
        player1 = createPlayer("@player1:behind.computer");
        player1.reset();
        player1.enqueueEvent(new RenameEvent("Игрок 1"));
        player2 = createPlayer("@player2:behind.computer");
        player2.reset();
        player2.enqueueEvent(new RenameEvent("Игрок 2"));
        player3 = createPlayer("@player3:behind.computer");
        player3.reset();
        player3.enqueueEvent(new RenameEvent("Игрок 3"));
        player4 = createPlayer("@player4:behind.computer");
        player4.reset();
        player4.enqueueEvent(new RenameEvent("Игрок 4"));
        player5 = createPlayer("@player5:behind.computer");
        player5.reset();
        player5.enqueueEvent(new RenameEvent("Игрок 5"));
        World.THIS.setState(GamePlayerState.PLAYING);
        World.THIS.stopTime();
    }

    private IMutablePlayer createPlayer(String id) {
        return World.THIS.getCurrentPlayer(new MatrixMessage(null, null, id, id)).flatMap(p -> p.as(MUTABLEPLAYER_OBJ)).orElse(null);
    }

    @Test
    public void testFat() {
        World.THIS.getTraitsRepository().getObjectById("fat").ifPresent(player1::attachEffect);
        assertEquals(9, (int) player1.getStat(ATK));
        assertEquals(9, (int) player1.getStat(DEF));
        assertEquals(7, (int) player1.getStat(PRT));
    }

    @Test
    public void testThin() {
        World.THIS.getTraitsRepository().getObjectById("thin").ifPresent(player1::attachEffect);
        assertEquals(8, (int) player1.getStat(ATK));
        assertEquals(12, (int) player1.getStat(DEF));
        assertEquals(4, (int) player1.getStat(PRT));
        assertEquals(35, (int) player1.getStat(HP));
    }

    @Test
    public void testBattle() {
        setDRN(3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
    }

    @Test
    public void testWeapon() {
        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        equipWeapon(player1, "gauntlet");
        assertEquals("gauntlet", player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
    }

    @Test
    public void testWeapon2() {
        setDRN(2, 2, 2, 2, 2, 2, 3, 2);
        equipWeapon(player1, "ironrod");
        assertEquals("ironrod", player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(28, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
    }

    @Test
    public void testVictory() {
        // only two players participate in this round
        World.THIS.setState(GamePlayerState.GATHER);
        World.THIS.setPlayerState(player3, GamePlayerState.NONE);
        World.THIS.setPlayerState(player4, GamePlayerState.NONE);
        World.THIS.setPlayerState(player5, GamePlayerState.NONE);
        World.THIS.setPlayerState(player1, GamePlayerState.READY);
        World.THIS.setPlayerState(player2, GamePlayerState.READY);
        // check if the game has begun
        assertEquals(GamePlayerState.PLAYING, World.THIS.getState());
        World.THIS.stopTime();
        setDRN(100, 2, 100, 2, 2, 100, 2, 100); // one-shot kill setup
        Integer hp = player1.getStat(HP);
        // to check for loot pickup, both backpack and equipped
        equipWeapon(player2, "gauntlet");
        pickupWeapon(player2, "ironrod");
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(hp, player1.getStat(HP));
        assertEquals(0, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
        // check deaths
        assertTrue(player1.isAlive());
        assertFalse(player2.isAlive());
        // check loot
        final List<IItem> backpack = player1.getBackpack();
        assertEquals(2, backpack.size());
        assertTrue(backpack.stream().anyMatch(i -> i.getType().equals("ironrod")));
        assertTrue(backpack.stream().anyMatch(i -> i.getType().equals("gauntlet")));
        assertEquals(0, player2.getBackpack().size());
        // check for victory, the game mode should switch to gather
        assertEquals(GamePlayerState.GATHER, World.THIS.getState());
    }

    @Test
    public void testHide() {
        setDRN(3, 3);
        player2.enqueueToggleEffect(new HideEffect());
        assertTrue(player2.hasEffect(HideEffect.TYPE));
        assertEquals(8, (int) player2.getStat(STM));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertTrue(player2.hasEffect(HideEffect.TYPE));

        player2.enqueueToggleEffect(new HideEffect());
        assertFalse(player2.hasEffect(HideEffect.TYPE));
        assertEquals(8, (int) player2.getStat(STM));

        player2.enqueueToggleEffect(new HideEffect());
        assertTrue(player2.hasEffect(HideEffect.TYPE));

        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(30, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(6, (int) player2.getStat(STM));
        setDRN(3, 4, 3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(HideEffect.TYPE));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        assertEquals(0, (int) player1.getStat(STM));
        assertEquals(6, (int) player2.getStat(STM));
    }

    @Test
    public void testAmbushFound() {
        setDRN(3, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        assertTrue(player2.hasEffect(AmbushEffect.TYPE));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(AmbushEffect.TYPE));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
    }

    @Test
    public void testAmbushSuccess() {
        setDRN(4, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        assertTrue(player2.hasEffect(AmbushEffect.TYPE));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(AmbushEffect.TYPE));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(30, (int) player2.getStat(HP));
    }

    @Test
    public void testStaminaRegenSuccess() {
        setDRN(2, 2, 2, 6);
        player1.enqueueEvent(new TickEvent());
        assertEquals(11, (int) player1.getStat(STM));
        player1.enqueueEvent(new TickEvent());
        assertEquals(11, (int) player1.getStat(STM));
    }

    @Test
    public void testGather() {
        World.THIS.setState(GamePlayerState.GATHER);
        World.THIS.setPlayerState(player1, GamePlayerState.GATHER);
        World.THIS.setPlayerState(player2, GamePlayerState.GATHER);
        World.THIS.setPlayerState(player3, GamePlayerState.GATHER);
        World.THIS.setPlayerState(player4, GamePlayerState.GATHER);
        World.THIS.setPlayerState(player5, GamePlayerState.NONE);
        // 4 gather, 3 ready, 4 should be playing
        World.THIS.setPlayerState(player1, GamePlayerState.READY);
        World.THIS.setPlayerState(player2, GamePlayerState.READY);
        World.THIS.setPlayerState(player3, GamePlayerState.READY);
        World.THIS.setPlayerState(player5, GamePlayerState.READY);
        assertEquals(GamePlayerState.PLAYING, World.THIS.getState());
        assertEquals(GamePlayerState.PLAYING, player1.getState());
        assertEquals(GamePlayerState.PLAYING, player2.getState());
        assertEquals(GamePlayerState.PLAYING, player3.getState());
        assertEquals(GamePlayerState.PLAYING, player4.getState());
        assertEquals(GamePlayerState.NONE, player5.getState());
    }

    @Test
    public void testWeaponSwap() {
        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        equipWeapon(player1, "gauntlet");
        assertEquals("gauntlet", player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        equipWeapon(player1, "ironrod");
        assertEquals("ironrod", player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        final List<IItem> backpack = player1.getBackpack();
        assertEquals(1, backpack.size());
        assertEquals("gauntlet", backpack.get(0).getType());
    }

    @Test
    public void testRecharge() {
        flushLogs();
        pickupItem(player1, "doshirakbeef");
        final UseCommand useCommand = new UseCommand();
        useCommand.exec(player1, Stream.of("1"));
        useCommand.exec(player1, Stream.of("1"));
        assertEquals(20, (int) player1.getStat(STM));
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        equipWeapon(player1, "lasersaw");
        assertTrue(player1.getWeapon().map(w -> w.hasEffect(ChargeableEffect.TYPE)).orElse(false));
        assertEquals(3, getWeaponCharges(player1));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(27, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
        assertEquals(0, getWeaponCharges(player1));
        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2)); // should deduce 1 hp when discharged
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(26, (int) player2.getStat(HP));
        assertEquals(0, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));

        pickupItem(player1, "doshirakbeef");
        useCommand.exec(player1, Stream.of("1"));
        useCommand.exec(player1, Stream.of("1"));
        assertEquals(10, (int) player1.getStat(STM));

        // pickup recharge item, check if it disappears on use
        pickupItem(player1, "energycell");
        assertEquals(1, (int) player1.getBackpack().get(0).as(ITEM_OBJ).flatMap(e -> e.getAttribute(USE_CNT)).orElse(-1));

        // unequip weapon and test if item use fails
        new UnequipCommand().exec(player1, Stream.of("о"));
        assertEquals(2, player1.getBackpack().size());
        assertEquals(Optional.empty(), player1.getWeapon());
        useCommand.exec(player1, Stream.of("1")); // should fail as no weapon is equipped
        assertEquals(2, player1.getBackpack().size());
        
        // check that charges aren't used and not applied to the weapon
        assertEquals(0, (int) player1.getBackpack().get(1).as(WEAPON_OBJ).flatMap(w -> w.getEffect(ChargeableEffect.TYPE))
                .flatMap(e -> e.getAttribute(ChargeableEffect.CHARGES)).orElse(-1));
        assertTrue(player1.getBackpack().get(0).as(ITEM_OBJ).map(i -> i.hasEffect(RechargeEffect.TYPE)).orElse(false));
        assertEquals(1, (int) player1.getBackpack().get(0).as(ITEM_OBJ).flatMap(e -> e.getAttribute(USE_CNT)).orElse(-1));
        
        // equip the weapon back and use the recharge
        new EquipCommand().exec(player1, Stream.of("2"));
        useCommand.exec(player1, Stream.of("1"));
        assertEquals(3, getWeaponCharges(player1));
        assertEquals(0, player1.getBackpack().size());

        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2)); // should deduce 3 hp when charged
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(23, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
    }

    private int getWeaponCharges(IMutablePlayer player) {
        return player.getWeapon().flatMap(w -> w.getEffect(ChargeableEffect.TYPE)).flatMap(e -> e.getAttribute(ChargeableEffect.CHARGES))
                .orElse(-1);
    }

    private void flushLogs() {
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

    private void pickupItem(IPlayer player, String type) {
        World.THIS.getUsableRepository().getObjectById(type).ifPresent(player::enqueuePickup);
    }

    protected void equipWeapon(IPlayer player, String type) {
        World.THIS.getWeaponRepository().getObjectById(type).ifPresent(player::enqueueEquipItem);
    }
}
