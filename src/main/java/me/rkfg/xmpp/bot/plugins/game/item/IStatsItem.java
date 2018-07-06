package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.IHasStats;

public interface IStatsItem extends IItem, IHasStats {

    @Override
    default Optional<String> getDescriptionWithParams() {
        return IItem.super.getDescription().map(d -> d + getStatsStr() + describeEffects());
    }

    String getStatsStr();

}
