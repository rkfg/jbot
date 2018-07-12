package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EquipEvent.EquippedEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent;
import me.rkfg.xmpp.bot.plugins.game.event.UnequipEvent.UnequippedEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class DeadEffect extends AbstractEffect {

    public static final String TYPE = "dead";

    public DeadEffect() {
        super(TYPE, "мёртв");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfAnyType(EquipEvent.TYPE, EquippedEvent.TYPE, UnequipEvent.TYPE, UnequippedEvent.TYPE)) {
            return noEvent();
        }
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
