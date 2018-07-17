package me.rkfg.xmpp.bot.plugins.game.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractEvent implements IEvent {

    protected IGameObject source;
    protected IGameObject target;
    protected Logger log = LoggerFactory.getLogger(getClass());
    TypedAttributeMap attrs = new TypedAttributeMap();

    private boolean cancelled = false;

    public AbstractEvent(String type) {
        setType(type);
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
    public void apply() {
        logTargetComment();
    }

    protected void logTargetComment() {
        getDescription().ifPresent(m -> target.log(m));
    }

    protected void logSourceComment() {
        getDescription().ifPresent(m -> source.log(m));
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
