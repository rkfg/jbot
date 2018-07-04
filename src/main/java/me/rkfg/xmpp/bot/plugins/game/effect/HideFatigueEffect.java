package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class HideFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "hidefatigue";

    public HideFatigueEffect() {
        super(TYPE, "устаёт от скрытности");
        initFatigue(6);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processEffectAttachFatigue(event, HideEffect.TYPE, "Вы слишком устали, чтобы надёжно спрятаться.");
    }

}
