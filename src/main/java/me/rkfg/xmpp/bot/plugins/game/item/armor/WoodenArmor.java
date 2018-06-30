package me.rkfg.xmpp.bot.plugins.game.item.armor;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;

public class WoodenArmor extends AbstractArmor {

    public WoodenArmor() {
        this(null);
    }
    
    public WoodenArmor(IGameObject owner) {
        super(owner, -1, 1);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("кусок фанеры");
    }

}
