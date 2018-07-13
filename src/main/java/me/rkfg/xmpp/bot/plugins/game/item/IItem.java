package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasAttributes;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasType;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public interface IItem extends IGameObject, IHasAttributes, IHasType {

    public enum Type {
        NONE, WEAPON, ARMOR, USABLE
    }

    Optional<TypedAttribute<ISlot>> getFittingSlot();

    Optional<IGameObject> getOwner();

    void setOwner(IGameObject owner);

    default String getItemDescription() {
        return getDescription().orElse("<предмет>");
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends IGameObject> Optional<T> as(TypedAttribute<T> type) {
        if (type == ITEM_OBJ) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    @Override
    default void log(String message) {
        getOwner().ifPresent(owner -> owner.log(message));
    }

    default void onUse() {

    }

    @Override
    default Optional<String> getDescriptionWithParams() {
        return Optional.of(getDescription().orElse("<нет описания>") + describeEffects());
    }

    default String describeEffects() {
        return listEffects().stream().filter(IEffect::isVisible).map(IEffect::getDescription).filter(Optional::isPresent).map(Optional::get)
                .reduce(commaReducer).map(s -> " [" + s + "]").orElse("");
    }

    default Type getItemType() {
        return Type.NONE;
    }

}
