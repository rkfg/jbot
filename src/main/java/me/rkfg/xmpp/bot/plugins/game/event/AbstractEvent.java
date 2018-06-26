package me.rkfg.xmpp.bot.plugins.game.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.AbstractAttributesStorage;

public abstract class AbstractEvent extends AbstractAttributesStorage implements IEvent {

    private String type;
    protected IGameObject source;
    protected IGameObject target;
    protected Logger log = LoggerFactory.getLogger(getClass());

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

}
