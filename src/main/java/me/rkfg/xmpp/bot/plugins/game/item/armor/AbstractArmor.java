package me.rkfg.xmpp.bot.plugins.game.item.armor;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.misc.IMutableStats;

public abstract class AbstractArmor extends AbstractItem implements IArmor, IMutableStats {

    public AbstractArmor(Integer defence, Integer protection, String description) {
        super(description);
        setAttribute(DEF, defence);
        setAttribute(PRT, protection);
    }

    @Override
    public Optional<String> getDescription() {
        return super.getDescription().map(d -> d + getStatsStr());
    }

    private String getStatsStr() {
        return String.format(" З:%d/Б:%d", getDefence(), getProtection());
    }
}
