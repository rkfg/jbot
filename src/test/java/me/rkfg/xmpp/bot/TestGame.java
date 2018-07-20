package me.rkfg.xmpp.bot;

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

import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class TestGame {

    private Player player1;
    private Player player2;

    private static Logger log = LoggerFactory.getLogger(TestGame.class);
    private Random randomMock;

    @BeforeAll
    static void initWorld() {
        try {
            Field field = Main.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, mock(IBot.class));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail(e);
        }
        World.THIS.init();
    }

    public void setRandom(List<Integer> numbers) {
        try {
            Field field = Utils.class.getDeclaredField("rnd");
            field.setAccessible(true);
            randomMock = Mockito.mock(Random.class);
            OngoingStubbing<Integer> stubbing = when(randomMock.nextInt(anyInt()));
            for (Integer n : numbers) {
                stubbing = stubbing.thenReturn(n);
            }
            field.set(null, randomMock);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            log.warn("{}", e);
            fail();
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
        player1 = new Player("@player1:behind.computer");
        player1.reset();
        player1.enqueueEvent(new RenameEvent("Игрок 1"));
        player2 = new Player("@player2:behind.computer");
        player2.reset();
        player2.enqueueEvent(new RenameEvent("Игрок 2"));
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
        dumpLogs();
    }

    @Test
    public void testHide() {
        setDRN(3, 3, 3, 4, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new HideEffect());
        assertEquals(8, (int) player2.getStat(STM));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(30, (int) player2.getStat(HP));
        assertEquals(5, (int) player1.getStat(STM));
        assertEquals(8, (int) player2.getStat(STM));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        assertEquals(0, (int) player1.getStat(STM));
        assertEquals(8, (int) player2.getStat(STM));
        dumpLogs();
    }

    @Test
    public void testAmbushFound() {
        setDRN(3, 3, 3, 3, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(30, (int) player1.getStat(HP));
        assertEquals(29, (int) player2.getStat(HP));
        dumpLogs();
    }

    @Test
    public void testAmbushSuccess() {
        setDRN(4, 3, 3, 3, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(29, (int) player1.getStat(HP));
        assertEquals(30, (int) player2.getStat(HP));
        dumpLogs();
    }

    @Test
    public void testStaminaRegenSuccess() {
        setDRN(2, 2, 2, 6);
        player1.enqueueEvent(new TickEvent());
        assertEquals(11, (int) player1.getStat(STM));
        player1.enqueueEvent(new TickEvent());
        assertEquals(11, (int) player1.getStat(STM));
    }

    private void dumpLogs() {
        log.info("Player 1 log: {}", player1.getLog());
        log.info("Player 2 log: {}", player2.getLog());
    }
}
