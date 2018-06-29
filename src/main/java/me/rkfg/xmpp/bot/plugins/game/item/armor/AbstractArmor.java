package me.rkfg.xmpp.bot.plugins.game.item.armor;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;

public abstract class AbstractArmor extends AbstractItem implements IArmor {

    public AbstractArmor(IGameObject owner, Integer defence, Integer protection) {
        super(owner);
        setAttribute(DEF, defence);
        setAttribute(PRT, protection);
    }
}
