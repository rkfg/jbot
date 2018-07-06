package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractItem extends AbstractEffectReceiver implements IItem {

    private IGameObject owner;
    private TypedAttributeMap attrs = new TypedAttributeMap();

    public AbstractItem(String description) {
        setAttribute(DESCRIPTION, description);
    }

    @Override
    public Optional<IGameObject> getOwner() {
        return Optional.ofNullable(owner);
    }

    @Override
    public void setOwner(IGameObject owner) {
        this.owner = owner;
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }

}
