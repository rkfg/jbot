package me.rkfg.xmpp.bot.plugins.game;

import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IPlayer extends IGameObject {

    boolean isAlive();

    String getId();

    String getName();

    void dumpStats();

    String getLog();

    Integer getStat(TypedAttribute<Integer> attr);

    Optional<ISlot> getSlot(TypedAttribute<ISlot> slot);

    Optional<IWeapon> getWeapon();

    Optional<IArmor> getArmor();

    List<IItem> getBackpack();

    boolean enqueueEquipItem(IItem item);

    boolean enqueueUnequipItem(TypedAttribute<ISlot> slot);
    
    boolean enqueuePickup(IItem item);
}
