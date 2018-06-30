package me.rkfg.xmpp.bot.plugins.game.item.armor;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;

public abstract class AbstractArmor extends AbstractItem implements IArmor {

    public AbstractArmor(IGameObject owner, Integer defence, Integer protection, String description) {
        super(owner, description);
        setAttribute(DEF, defence);
        setAttribute(PRT, protection);
    }

    @Override
    public Optional<String> getDescription() {
        return super.getDescription().map(d -> d + getStatsStr());
    }

    private String getStatsStr() {
        return String.format(" %d/%d", getDefence(), getProtection());
    }
}
