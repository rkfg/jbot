package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.AbstractAttributesStorage;

public abstract class AbstractEffect extends AbstractAttributesStorage implements IEffect {
    private String name;
    protected IGameObject source;
    protected IGameObject target;
    private String localizedName;

    public AbstractEffect(String name, String localizedName, IGameObject source) {
        this.name = name;
        this.localizedName = localizedName;
        this.source = source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocalizedName() {
        return localizedName;
    }

    @Override
    public void onAttach() {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public boolean isReplacementAllowed(IEffect replacement) {
        return true;
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return Collections.emptySet();
    }

    @Override
    public IGameObject getSource() {
        return source;
    }

    @Override
    public IGameObject getTarget() {
        return target;
    }

    @Override
    public void setTarget(IGameObject target) {
        this.target = target;
    }

    protected Collection<IEvent> singleEvent(IEvent event) {
        return Arrays.asList(event);
    }
}
