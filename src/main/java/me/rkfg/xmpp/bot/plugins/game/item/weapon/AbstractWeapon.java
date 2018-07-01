package me.rkfg.xmpp.bot.plugins.game.item.weapon;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;

public abstract class AbstractWeapon extends AbstractItem implements IWeapon {

    public AbstractWeapon(Integer attack, Integer defence, Integer strength, String description) {
        super(description);
        setAttribute(ATK, attack);
        setAttribute(DEF, defence);
        setAttribute(STR, strength);
    }

    @Override
    public Optional<String> getDescription() {
        return super.getDescription().map(d -> d + getStatsStr());
    }

    private String getStatsStr() {
        return String.format(" %d/%d/%d", getAttack(), getDefence(), getStrength());
    }
}
