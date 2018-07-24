package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;

public class SearchFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "searchfatigue";

    public SearchFatigueEffect(int stmCost) {
        super(TYPE, "устаёт при поисках");
        initFatigue(stmCost);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processFatigue(event, SearchEvent.TYPE, "Вы слишком устали, чтобы искать вещи.");
    }

}
