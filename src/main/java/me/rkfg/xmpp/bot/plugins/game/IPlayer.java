package me.rkfg.xmpp.bot.plugins.game;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.event.ItemPickupEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasStats;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasTraits;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IPlayer extends IGameObject, IHasStats, IHasTraits {

    boolean isAlive();

    String getId();

    String getRoomId();

    String getName();

    void dumpStats();

    String getLog();

    Optional<ISlot> getSlot(TypedAttribute<ISlot> slot);

    default Optional<IWeapon> getWeapon() {
        return getSlot(WEAPON_SLOT).flatMap(ISlot::getItem).flatMap(i -> i.as(WEAPON_OBJ));
    }

    default Optional<IArmor> getArmor() {
        return getSlot(ARMOR_SLOT).flatMap(ISlot::getItem).flatMap(i -> i.as(ARMOR_OBJ));
    }

    default String getWeaponName() {
        return getWeapon().map(IWeapon::getItemDescription).orElse("кулаки");
    }

    default String getArmorName() {
        return getArmor().map(IArmor::getItemDescription).orElse("куртку");
    }

    List<IItem> getBackpack();

    boolean enqueueEquipItem(IItem item);

    boolean enqueueUnequipItem(TypedAttribute<ISlot> slot);

    default boolean enqueuePickup(IItem item) {
        return enqueueEvent(new ItemPickupEvent(item));
    }

    GamePlayerState getState();

    void flushLogs();

}
