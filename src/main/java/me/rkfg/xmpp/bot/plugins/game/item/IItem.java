package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IItem extends IGameObject, IHasDescription, IHasAttributes {
    Optional<TypedAttribute<ISlot>> getFittingSlot();

    IGameObject getOwner();

    void setOwner(IGameObject owner);
}
