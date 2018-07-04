package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;

public class SearchFatigue extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "searchfatigue";

    public SearchFatigue() {
        super(TYPE, "устаёт при поисках");
        initFatigue(2);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processFatigue(event, SearchEvent.TYPE, "Вы слишком устали, чтобы искать вещи.");
    }

}
