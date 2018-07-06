package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;

public interface ISlot extends IGameBase, IHasDescription {
    public Optional<IItem> getItem();
}
