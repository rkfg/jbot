package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IItem extends IGameObject, IHasDescription, IHasAttributes {
    Optional<TypedAttribute<ISlot>> getFittingSlot();

    Optional<IGameObject> getOwner();

    void setOwner(IGameObject owner);

    @SuppressWarnings("unchecked")
    @Override
    default <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == ITEM_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    default void onUse() {
        
    }
    
}
