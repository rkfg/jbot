package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class AmbushFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "ambushfatigue";

    public AmbushFatigueEffect() {
        super(TYPE, "устаёт от засады");
        initFatigue(7);
    }
    
    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processEffectAttachFatigue(event, AmbushEffect.TYPE, "Вы слишком устали, чтобы сидеть в засаде.");
    }
    
}
