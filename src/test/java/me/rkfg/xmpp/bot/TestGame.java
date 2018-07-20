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
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.MatrixMessage;
import me.rkfg.xmpp.bot.plugins.game.IMutablePlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
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

    @BeforeAll
    static void initWorld() {
        try {
            Field field = Main.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, mock(IBot.class));
            Field rndField = Utils.class.getDeclaredField("rnd");
            rndField.setAccessible(true);
            randomMock = Mockito.mock(Random.class);
            rndField.set(null, randomMock);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail(e);
        }
        World.THIS.init();
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
    public void testVictory() {
        setDRN(3, 2, 40, 2, 3, 2, 3, 2);
        World.THIS.getWeaponRepository().getObjectById("ironrod").ifPresent(player2::enqueuePickup);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(0, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(10, (int) player2.getStat(STM));
        assertTrue(player1.isAlive());
        assertFalse(player2.isAlive());
        assertEquals(1, player1.getBackpack().size());
        assertEquals(0, player2.getBackpack().size());
    }

    @Test
    public void testHide() {
        setDRN(3, 3);
        player2.enqueueToggleEffect(new HideEffect());
        assertEquals(8, (int) player2.getStat(STM));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(30, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(8, (int) player2.getStat(STM));
        setDRN(3, 4, 3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        assertEquals(0, (int) player1.getStat(STM));
        assertEquals(8, (int) player2.getStat(STM));
    }

    @Test
    public void testAmbushFound() {
        setDRN(3, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
    }

    @Test
    public void testAmbushSuccess() {
        setDRN(4, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        player1.enqueueEvent(new BattleEvent(player1, player2));
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

    protected void dumpLogs() {
        log.info("Player 1 log: {}", player1.getLog());
        log.info("Player 2 log: {}", player2.getLog());
    }
}
