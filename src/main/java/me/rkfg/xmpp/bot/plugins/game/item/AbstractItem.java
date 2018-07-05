package me.rkfg.xmpp.bot.plugins.game.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffectReceiver;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractItem extends AbstractEffectReceiver implements IItem {

    private IGameObject owner;
    private TypedAttributeMap attrs = new TypedAttributeMap();
    private String description;

    public AbstractItem(String description) {
        this.description = description;
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

    @Override
    public Optional<String> getDescription() {
        return Optional.of((description != null ? description : "<нет описания>")
                + listEffects().stream().filter(IEffect::isVisible).map(IEffect::getDescription).filter(Optional::isPresent)
                        .map(Optional::get).reduce(commaReducer).map(s -> " [" + s + "]").orElse(""));
    }

}
