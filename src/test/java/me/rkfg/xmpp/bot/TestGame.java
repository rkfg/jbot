package me.rkfg.xmpp.bot;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.command.EquipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.RebuildItemsCommand;
import me.rkfg.xmpp.bot.plugins.game.command.SpendPointsCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UnequipCommand;
import me.rkfg.xmpp.bot.plugins.game.command.UseCommand;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.CowardEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.HideEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StaminaRegenEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.ChargeableEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.RechargeEffect;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RenameEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;
import me.rkfg.xmpp.bot.plugins.game.event.TickEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;

public class TestGame extends TestBase {

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

    @Test
    public void testFat() {
        applyTrait(player1, "fat");
        assertStatChange(player1, ATK, -1);
        assertStatChange(player1, DEF, -1);
        assertStatChange(player1, PRT, 2);
    }

    @Test
    public void testThin() {
        applyTrait(player1, "thin");
        assertStatChange(player1, ATK, -2);
        assertStatChange(player1, DEF, 2);
        assertStatChange(player1, PRT, -1);
        assertStatChange(player1, HP, 5);
    }

    @Test
    public void testBattle() {
        setDRN(3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -1);
        assertStatChange(player1, STM, -5);
        assertStatChange(player2, STM, 0);
    }

    @Test
    public void testWeapon() {
        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        equipWeapon(player1, W_GAUNTLET);
        pickupItem(player2, U_ENERGYCELL);
        assertEquals(W_GAUNTLET, player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, -1);
        assertBattleSTMChange(player1, 1);
        assertStatChange(player2, STM, 0);
        // check that looting wasn't triggered accidentally
        assertEquals(0, (int) player1.getBackpack().size());
    }

    @Test
    public void testWeapon2() {
        setDRN(2, 2, 2, 2, 2, 2, 3, 2);
        equipWeapon(player1, W_IRONROD);
        assertEquals(W_IRONROD, player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -2);
        assertBattleSTMChange(player1, 1);
        assertBattleSTMChange(player2, 0);
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
        equipWeapon(player2, W_GAUNTLET);
        pickupWeapon(player2, W_IRONROD);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertEquals(hp, player1.getStat(HP));
        assertEquals(0, (int) player2.getStat(HP));
        assertBattleSTMChange(player1, 1);
        assertBattleSTMChange(player2, 0);
        // check deaths
        assertTrue(player1.isAlive());
        assertFalse(player2.isAlive());
        // check loot
        final List<IItem> backpack = player1.getBackpack();
        assertEquals(2, backpack.size());
        assertTrue(backpack.stream().anyMatch(i -> i.getType().equals(W_IRONROD)));
        assertTrue(backpack.stream().anyMatch(i -> i.getType().equals(W_GAUNTLET)));
        assertEquals(0, player2.getBackpack().size());
        // check for victory, the game mode should switch to gather
        assertEquals(GamePlayerState.GATHER, World.THIS.getState());
        player1.enqueueEvent(new TickEvent());
        player2.enqueueEvent(new TickEvent());
        assertStatChange(player1, STM, -Player.BATTLE_FATIGUE_COST + 1);
        assertStatChange(player2, STM, 0);
    }

    @Test
    public void testHide() {
        setDRN(3, 3);
        player2.enqueueToggleEffect(new HideEffect());
        assertTrue(player2.hasEffect(HideEffect.TYPE));
        assertStatChange(player2, STM, -2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertTrue(player2.hasEffect(HideEffect.TYPE));

        player2.enqueueToggleEffect(new HideEffect());
        assertFalse(player2.hasEffect(HideEffect.TYPE));
        assertStatChange(player2, STM, -2);

        player2.enqueueToggleEffect(new HideEffect());
        assertTrue(player2.hasEffect(HideEffect.TYPE));

        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, 0);
        assertBattleSTMChange(player1, 1);
        assertStatChange(player2, STM, -4);
        setDRN(3, 4, 3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(HideEffect.TYPE));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -1);
        assertBattleSTMChange(player1, 2);
        assertStatChange(player2, STM, -4);
    }

    @Test
    public void testAmbushFound() {
        setDRN(3, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        assertTrue(player2.hasEffect(AmbushEffect.TYPE));
        assertStatChange(player2, STM, -Player.AMBUSH_FATIGUE_COST);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(AmbushEffect.TYPE));
        assertBattleSTMChange(player1, 1);
        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, -1);
    }

    @Test
    public void testAmbushSuccess() {
        setDRN(4, 3, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2);
        player2.enqueueToggleEffect(new AmbushEffect());
        assertTrue(player2.hasEffect(AmbushEffect.TYPE));
        assertStatChange(player2, STM, -Player.AMBUSH_FATIGUE_COST);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player2.hasEffect(AmbushEffect.TYPE));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, 0);
    }

    @Test
    public void testStaminaRegenSuccess() {
        setDRN(3, 2, 2, 6);
        player1.enqueueEvent(new TickEvent());
        assertEquals(Player.BASE_STM + 1, (int) player1.getStat(STM));
        player1.enqueueEvent(new TickEvent());
        assertEquals(Player.BASE_STM + 1, (int) player1.getStat(STM));
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
        equipWeapon(player1, W_GAUNTLET);
        assertEquals(W_GAUNTLET, player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        equipWeapon(player1, W_IRONROD);
        assertEquals(W_IRONROD, player1.getWeapon().map(IWeapon::getType).orElseGet(() -> fail("no weapon")));
        final List<IItem> backpack = player1.getBackpack();
        assertEquals(1, backpack.size());
        assertEquals(W_GAUNTLET, backpack.get(0).getType());
    }

    @Test
    public void testRecharge() {
        flushLogs();
        pickupItem(player1, U_DOSHIRAKBEEF);
        final UseCommand useCommand = new UseCommand();
        useCommand.exec(player1, Stream.of("1"));
        useCommand.exec(player1, Stream.of("1"));
        assertStatChange(player1, STM, 10);
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        equipWeapon(player1, W_LASERSAW);
        assertTrue(player1.getWeapon().map(w -> w.hasEffect(ChargeableEffect.TYPE)).orElse(false));
        assertEquals(3, getWeaponCharges(player1));
        player1.enqueueEvent(new BattleEvent(player1, player2));
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        setDRN(2, 3, 2, 4, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, -3);
        assertStatChange(player1, STM, 10 - Player.BATTLE_FATIGUE_COST * 3);
        assertStatChange(player2, STM, 0);
        assertEquals(0, getWeaponCharges(player1));
        setDRN(2, 2, 2, 2, 2, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2)); // should deduce 1 hp when discharged
        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, -4);
        assertStatChange(player1, STM, 10 - Player.BATTLE_FATIGUE_COST * 4);
        assertStatChange(player2, STM, 0);

        pickupItem(player1, U_DOSHIRAKBEEF);
        useCommand.exec(player1, Stream.of("1"));
        useCommand.exec(player1, Stream.of("1"));
        assertStatChange(player1, STM, 20 - Player.BATTLE_FATIGUE_COST * 4);

        // pickup recharge item, check if it disappears on use
        pickupItem(player1, U_ENERGYCELL);
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
        assertStatChange(player1, HP, 0);
        assertStatChange(player2, HP, -7);
        assertStatChange(player1, STM, 20 - Player.BATTLE_FATIGUE_COST * 5);
        assertStatChange(player2, STM, 0);
    }

    @Test
    public void testItemSet() {
        equipWeapon(player1, W_CONSOLE);
        assertEquals(2, (int) player1.getWeapon().map(w -> w.getStat(ATK)).orElse(0));
        setDRN(2, 3, 2, 2, 4, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2)); // should deduce 2 hp
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -2);
        assertBattleSTMChange(player1, 1);
        assertBattleSTMChange(player2, 0);
        equipArmor(player1, A_CHEATS);
        assertEquals(3, (int) player1.getWeapon().map(w -> w.getStat(ATK)).orElse(0));
        assertEquals(5, (int) player1.getArmor().map(a -> a.getStat(DEF)).orElse(0));

        // try set in battle
        setDRN(2, 4, 2, 2, 7, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -4);
        assertBattleSTMChange(player1, 2);
        assertBattleSTMChange(player2, 0);

        player1.changeAttribute(STM, 5);
        setDRN(2, 4, 2, 2, 8, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -6);
        assertBattleSTMChange(player1, 2);
        assertBattleSTMChange(player2, 0);

        player1.enqueueUnequipItem(WEAPON_SLOT);
        assertEquals(4, (int) player1.getArmor().map(a -> a.getStat(DEF)).orElse(0));
        equipWeapon(player1, W_CONSOLE);
        assertEquals(5, (int) player1.getArmor().map(a -> a.getStat(DEF)).orElse(0));
        assertEquals(3, (int) player1.getWeapon().map(w -> w.getStat(ATK)).orElse(0));
        player1.enqueueUnequipItem(ARMOR_SLOT);
        assertEquals(2, (int) player1.getWeapon().map(w -> w.getStat(ATK)).orElse(0));
    }

    @Test
    public void testBazookasNoWeapon() {
        applyTrait(player1, "bazookahands");
        assertStatChange(player1, ATK, 0);
        assertStatChange(player1, STR, 0);
        setDRN(2, 3, 2, 3, 3, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, -1);
        assertStatChange(player1, ATK, 0);
        assertStatChange(player1, STR, 0);
    }

    @Test
    public void testBazookasWithWeapon() {
        applyTrait(player1, "bazookahands");
        equipWeapon(player1, W_GAUNTLET);
        assertStatChange(player1, ATK, 0);
        assertStatChange(player1, STR, 0);
        setDRN(2, 3, 2, 3, 3, 2, 2, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertStatChange(player1, HP, -1);
        assertStatChange(player2, HP, 0);
        assertStatChange(player1, ATK, 0);
        assertStatChange(player1, STR, 0);
    }

    @Test
    public void testSpendBonusPoints() {
        SpendPointsCommand command = new SpendPointsCommand();
        defaultStats();
        command.exec(player1, Stream.of("ааа", "зп", "п"));
        defaultStats();
        command.exec(player1, Stream.of(""));
        defaultStats();
        command.exec(player1, Stream.of("аа", "зп"));
        assertBonusPointsSpent(3);
        assertStatChange(player1, ATK, 2);
        assertStatChange(player1, DEF, 1);
        assertStatChange(player1, STR, 0);
        assertStatChange(player1, PRT, 0);
        assertStatChange(player1, LCK, 0);
        assertStatChange(player1, HP, 0);
        assertStatChange(player1, STM, 0);
        command.exec(player1, Stream.of("с", "б"));
        fullySpent();
        command.exec(player1, Stream.of("с"));
        fullySpent();
    }

    @Test
    public void testBonusPointsExpiration() {
        SpendPointsCommand command = new SpendPointsCommand();
        player1.detachEffect(StaminaRegenEffect.TYPE); // turn off stamina regen to prevent test failure due to ticks
        defaultStats();
        for (int i = 0; i < Player.BONUS_EXPIRATON_TICKS; ++i) {
            player1.enqueueEvent(new TickEvent());
        }
        command.exec(player1, Stream.of("ааа", "зз"));
        defaultStats();
        assertBonusPointsSpent(5);
    }

    @Test
    public void testBonusPointsExpirationPartial() {
        SpendPointsCommand command = new SpendPointsCommand();
        player1.detachEffect(StaminaRegenEffect.TYPE); // turn off stamina regen to prevent test failure due to ticks
        defaultStats();
        command.exec(player1, Stream.of("аз"));
        assertStatChange(player1, ATK, 1);
        assertStatChange(player1, DEF, 1);
        assertStatChange(player1, STR, 0);
        assertStatChange(player1, PRT, 0);
        assertStatChange(player1, LCK, 0);
        assertStatChange(player1, HP, 0);
        assertStatChange(player1, STM, 0);
        assertBonusPointsSpent(2);
        for (int i = 0; i < Player.BONUS_EXPIRATON_TICKS; ++i) {
            player1.enqueueEvent(new TickEvent());
        }
        command.exec(player1, Stream.of("ааа", "зз"));
        assertStatChange(player1, ATK, 1);
        assertStatChange(player1, DEF, 1);
        assertStatChange(player1, STR, 0);
        assertStatChange(player1, PRT, 0);
        assertStatChange(player1, LCK, 0);
        assertStatChange(player1, HP, 0);
        assertStatChange(player1, STM, 0);
        assertBonusPointsSpent(5);
    }

    private void assertBonusPointsSpent(int pts) {
        assertEquals(Player.BASE_BONUS_POINTS - pts, (int) player1.getAttribute(BONUS_POINTS).orElse(-1));
    }

    public void fullySpent() {
        assertBonusPointsSpent(5);
        assertStatChange(player1, ATK, 2);
        assertStatChange(player1, DEF, 1);
        assertStatChange(player1, STR, 1);
        assertStatChange(player1, PRT, 1);
        assertStatChange(player1, LCK, 0);
        assertStatChange(player1, HP, 0);
        assertStatChange(player1, STM, 0);
    }

    public void defaultStats() {
        assertStatChange(player1, ATK, 0);
        assertStatChange(player1, DEF, 0);
        assertStatChange(player1, STR, 0);
        assertStatChange(player1, PRT, 0);
        assertStatChange(player1, LCK, 0);
        assertStatChange(player1, HP, 0);
        assertStatChange(player1, STM, 0);
    }

    @Test
    public void testSearch() {
        setRandom(randomMock, 2);
        setRandom(searchRandomMock, 0);
        assertEquals("buffout", SearchEvent.getRandomItem(SearchEvent.getRepo(3), 4).map(IItem::getType).orElse(""));
    }

    @Test
    public void testRebuildSameType() {
        pickupWeapon(player1, W_CONSOLE);
        pickupWeapon(player1, W_IRONROD);
        new RebuildItemsCommand().exec(player1, Stream.of("1", "2"));
        List<IItem> backpack = player1.getBackpack();
        assertEquals(1, backpack.size());
        assertEquals("lasersaw", backpack.get(0).getType());
    }

    @Test
    public void testRebuildDiffTypes() {
        setRandom(randomMock, 2); // will choose usable repo
        setRandom(searchRandomMock, 1); // will choose second usable item
        pickupWeapon(player1, W_CONSOLE);
        pickupArmor(player1, A_CHEATS);
        new RebuildItemsCommand().exec(player1, Stream.of("1", "2"));
        List<IItem> backpack = player1.getBackpack();
        assertEquals(1, backpack.size());
        assertEquals("doshirakbeef", backpack.get(0).getType());
    }

    @Test
    public void testCowardEffect() {
        setRandom(searchRandomMock, 0);
        setRandom(randomMock, 0);
        player2.setAttribute(HP, 10);
        for (int i = 0; i < StaminaRegenEffect.IDLE_LIMIT - 1; ++i) {
            player1.enqueueEvent(new TickEvent());
            player2.enqueueEvent(new TickEvent());
        }
        assertFalse(player1.hasEffect(CowardEffect.TYPE));
        assertFalse(player2.hasEffect(CowardEffect.TYPE));
        player1.enqueueEvent(new TickEvent());
        player2.enqueueEvent(new TickEvent());
        assertTrue(player1.hasEffect(CowardEffect.TYPE));
        assertTrue(player2.hasEffect(CowardEffect.TYPE));
        for (int i = 0; i < 20; ++i) {
            player1.enqueueEvent(new TickEvent());
            player2.enqueueEvent(new TickEvent());
        }
        assertEquals(30, (int) player1.getEffect(CowardEffect.TYPE).flatMap(e -> e.getAttribute(CowardEffect.COWARD_PTS)).orElse(-1));
        player1.enqueueEvent(new SearchEvent());
        assertEquals(0, player1.getBackpack().size());
        assertFalse(player1.getArmor().isPresent());
        assertFalse(player1.getWeapon().isPresent());
        setDRN(3, 2, 3, 2, 3, 2, 3, 2);
        player1.enqueueEvent(new BattleEvent(player1, player2));
        assertFalse(player1.hasEffect(CowardEffect.TYPE));
        assertTrue(player2.hasEffect(CowardEffect.TYPE));
        setRandom(searchRandomMock, 0);
        setRandom(randomMock, 0);
        player1.enqueueEvent(new SearchEvent());
        assertEquals("console", player1.getWeapon().map(IWeapon::getType).orElse(""));

        assertEquals(0, (int) player2.getEffect(CowardEffect.TYPE).flatMap(e -> e.getAttribute(CowardEffect.COWARD_PTS)).orElse(-1));
        setRandom(searchRandomMock, 0);
        setRandom(randomMock, 0);
        player2.enqueueEvent(new SearchEvent());
        assertEquals("console", player2.getWeapon().map(IWeapon::getType).orElse(""));
    }

    @Test
    public void testItemDrop() {
        pickupItem(player1, "godsgift");
        setRandom(searchRandomMock, 0);
        new UseCommand().exec(player1, Stream.of("1"));
        assertHasBackpackItem(player1, 0, A_CHEATS);
        assertHasBackpackItem(player2, 0, A_CHEATS);
        assertHasBackpackItem(player3, 0, A_CHEATS);
        assertHasBackpackItem(player4, 0, A_CHEATS);
        assertHasBackpackItem(player5, 0, A_CHEATS);
        pickupItem(player1, "devilsgift");
        new UseCommand().exec(player1, Stream.of("2"));
        assertHasBackpackItem(player1, 1, W_LASERSAW);
        assertHasBackpackItem(player2, 1, W_LASERSAW);
        assertHasBackpackItem(player3, 1, W_LASERSAW);
        assertHasBackpackItem(player4, 1, W_LASERSAW);
        assertHasBackpackItem(player5, 1, W_LASERSAW);
    }

}
