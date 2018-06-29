package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractItem extends AbstractEffectReceiver implements IItem {

    private IGameObject owner;
    private TypedAttributeMap attrs = new TypedAttributeMap();

    public AbstractItem(IGameObject owner) {
        if (owner == null) {
            owner = World.THIS;
        }
        this.owner = owner;
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == ITEM_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }
    
}
