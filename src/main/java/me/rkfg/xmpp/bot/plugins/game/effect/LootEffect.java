package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.ItemPickupEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class LootEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "loot";

    public LootEffect() {
        super(TYPE, "лутает убитого соперника после боя");
    }

    @Override
    public Collection<IEvent> battleWon(IPlayer defeated) {
        // try to unequip everything before looting
        defeated.enqueueUnequipItem(WEAPON_SLOT);
        defeated.enqueueUnequipItem(ARMOR_SLOT);
        defeated.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
            List<IItem> backpack = new ArrayList<>(defeated.getBackpack()); // copy to prevent concurrent modification
            backpack.stream().map(IItem::getDescription).filter(Optional::isPresent).map(Optional::get).reduce(commaReducer).ifPresent(
                    s -> target.as(PLAYER_OBJ).ifPresent(k -> p.log("%s победил вас и забирает все ваши вещи: %s.", k.getName(), s)));
            backpack.forEach(i -> {
                target.enqueueEvent(new ItemPickupEvent(i));
                p.removeFromBackpack(i);
            });
        });
        return noEvent();
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
