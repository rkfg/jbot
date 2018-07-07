package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.IMutableStats;

public abstract class AbstractWeapon extends AbstractItem implements IWeapon, IMutableStats {

    public AbstractWeapon(String type, Integer attack, Integer defence, Integer strength, String description) {
        super(type, description);
        setAttribute(ATK, attack);
        setAttribute(DEF, defence);
        setAttribute(STR, strength);
    }

    @Override
    public String getStatsStr() {
        return String.format(" А:%d/З:%d/С:%d", getAttack(), getDefence(), getStrength());
    }
}
