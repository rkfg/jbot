package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.IMutableStats;

public abstract class AbstractArmor extends AbstractItem implements IArmor, IMutableStats {

    public AbstractArmor(String type, Integer defence, Integer protection, String description) {
        super(type, description);
        setAttribute(DEF, defence);
        setAttribute(PRT, protection);
    }

    @Override
    public String getStatsStr() {
        return String.format(" З:%d/Б:%d", getDefence(), getProtection());
    }
}
