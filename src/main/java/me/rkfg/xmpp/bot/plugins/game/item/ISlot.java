package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;

public interface ISlot extends IGameBase {
    public Optional<IItem> getItem();
}
