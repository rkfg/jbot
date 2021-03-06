package me.rkfg.xmpp.bot.plugins.game;

import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.misc.Attrs.GamePlayerState;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IMutablePlayer extends IPlayer {

    void setId(String id);

    void setName(String name);
    
    void setRoomId(String roomId);

    void setDead(boolean dead);

    void equipItem(IItem item);

    void unequipItem(TypedAttribute<ISlot> slotAttr);

    void putItemToBackpack(IItem item);

    void removeFromBackpack(IItem item);

    void reset();
    
    void setState(GamePlayerState state);
}
