package me.rkfg.xmpp.bot.plugins.game.effect;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractEffect implements IEffect {

    private String type;
    protected IGameObject source;
    protected IGameObject target;
    TypedAttributeMap attrs = new TypedAttributeMap();

    public AbstractEffect(String type, String description) {
        this.type = type;
        setDescription(description);
    }

    @Override
    public String getType() {
        return type;
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
