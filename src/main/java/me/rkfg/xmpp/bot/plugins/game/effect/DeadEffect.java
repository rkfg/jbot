package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class DeadEffect extends AbstractEffect {

    public static final String TYPE = "dead";

    public DeadEffect() {
        super(TYPE, "мёртв");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        target.as(PLAYER_OBJ).ifPresent(p -> event.getSource().log("%s мёртв.", p.getName()));
        return cancelEvent(); // dead man reacts to no events
    }

    @Override
    public void onBeforeAttach() {
        World.THIS.announce(Utils.getPlayerName(target) + " погиб!");
        target.log("Вы умерли.");
    }

    @Override
    public void onAfterAttach() {
        World.THIS.checkVictory();
    }

    @Override
    public void onAfterDetach() {
        World.THIS.announce(Utils.getPlayerName(target) + " воскрес!");
        target.log("Вы воскресли.");
    }

}
