package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.repository.IContentRepository;

public interface IUsable extends IItem {

    @Override
    default Optional<TypedAttribute<ISlot>> getFittingSlot() {
        return Optional.of(ITEM_SLOT);
    }

    @Override
    default void onUse() {
        decAttribute(USE_CNT, () -> {
            getOwner().flatMap(o -> o.as(MUTABLEPLAYER_OBJ)).ifPresent(p -> p.removeFromBackpack(this));
            return null;
        });
    }
    
    @Override
    default Type getItemType() {
        return Type.USABLE;
    }
    
    @Override
    default Optional<IContentRepository> getContentRepository() {
        return Optional.of(World.THIS.getUsableRepository());
    }
}
