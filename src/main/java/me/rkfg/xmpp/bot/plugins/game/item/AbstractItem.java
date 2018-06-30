package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractItem extends AbstractEffectReceiver implements IItem {

    private IGameObject owner;
    private TypedAttributeMap attrs = new TypedAttributeMap();
    private String description;

    public AbstractItem(IGameObject owner, String description) {
        if (owner == null) {
            owner = World.THIS;
        }
        this.owner = owner;
        this.description = description;
    }

    public IGameObject getOwner() {
        return owner;
    }

    public void setOwner(IGameObject owner) {
        this.owner = owner;
    }

    @Override
    public void log(String message) {
        owner.log(message); // redirect item's log to the owner
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }
    
    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
    
}
