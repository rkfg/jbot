package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class DeadEffect extends AbstractEffect {

    public static final String TYPE = "dead";

    public DeadEffect() {
        super(TYPE, "мёртв", World.THIS);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return cancelEvent(); // dead man reacts to no events
    }

    @Override
    public void onAttach() {
        World.THIS.announce(Utils.getPlayerName(target) + " погиб!");
        target.log("Вы умерли.");
    }

    @Override
    public void onDetach() {
        World.THIS.announce(Utils.getPlayerName(target) + " воскрес!");
        target.log("Вы воскресли.");
    }

}
