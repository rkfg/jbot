package me.rkfg.xmpp.bot.plugins.game.item.weapon;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public class StickWeapon extends AbstractWeapon {

    public StickWeapon() {
        this(null);
    }

    public StickWeapon(IGameObject owner) {
        super(owner, 1, 1, 1, "палка-копалка");
    }

}
