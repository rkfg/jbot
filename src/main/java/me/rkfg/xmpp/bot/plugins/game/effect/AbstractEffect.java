package me.rkfg.xmpp.bot.plugins.game.effect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractEffect implements IEffect {

    protected IGameObject source;
    protected IGameObject target;
    protected Logger log = LoggerFactory.getLogger(getClass());
    TypedAttributeMap attrs = new TypedAttributeMap();

    public AbstractEffect(String type, String description) {
        setType(type);
        setDescription(description);
    }

    @Override
    public IGameObject getSource() {
        return source;
    }

    @Override
    public void setSource(IGameObject source) {
        this.source = source;
    }

    @Override
    public IGameObject getTarget() {
        return target;
    }

    @Override
    public void setTarget(IGameObject target) {
        this.target = target;
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }
}
