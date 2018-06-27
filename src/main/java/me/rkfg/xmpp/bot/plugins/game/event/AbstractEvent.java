package me.rkfg.xmpp.bot.plugins.game.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractEvent implements IEvent {

    public static final TypedAttribute<String> COMMENT = TypedAttribute.of("comment");

    private String type;
    protected IGameObject source;
    protected IGameObject target;
    protected Logger log = LoggerFactory.getLogger(getClass());
    TypedAttributeMap attrs = new TypedAttributeMap();

    public AbstractEvent(String type, IGameObject source) {
        this.type = type;
        this.source = source;
    }

    @Override
    public String getType() {
        return type;
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
    public void apply() {
        getAttribute(COMMENT).ifPresent(m -> target.log(m));
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }
}
