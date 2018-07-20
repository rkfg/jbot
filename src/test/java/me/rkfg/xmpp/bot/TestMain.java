package me.rkfg.xmpp.bot;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.security.SecureRandom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class TestMain {

    private Player player1;
    private Player player2;

    private static Logger log = LoggerFactory.getLogger(TestMain.class);

    @BeforeAll
    static void initWorld() {
        World.THIS.init();
        try {
            Field field = Utils.class.getDeclaredField("rnd");
            field.setAccessible(true);
            field.set(null, Mockito.mock(SecureRandom.class));
            int res = drn();
            log.info("Res={}", res);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            log.warn("{}", e);
        }
    }

    @BeforeEach
    void init() {
        player1 = new Player("@player1:behind.computer");
        player1.reset();
        player2 = new Player("@player2:behind.computer");
        player2.reset();
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
}
