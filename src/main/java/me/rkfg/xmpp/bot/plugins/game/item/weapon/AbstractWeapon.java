package me.rkfg.xmpp.bot.plugins.game.item.weapon;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;

public abstract class AbstractWeapon extends AbstractItem implements IWeapon {

    public AbstractWeapon(IGameObject owner, Integer attack, Integer defence, Integer strength) {
        super(owner);
        setAttribute(ATK, attack);
        setAttribute(DEF, defence);
        setAttribute(STR, strength);
    }
}
