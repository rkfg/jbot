package me.rkfg.xmpp.bot.plugins.game.item.armor;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public class WoodenArmor extends AbstractArmor {

    public WoodenArmor() {
        this(null);
    }
    
    public WoodenArmor(IGameObject owner) {
        super(owner, -1, 1, "кусок фанеры");
    }

}
