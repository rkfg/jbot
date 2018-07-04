package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class AmbushEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "ambush";

    public AmbushEffect() {
        super(TYPE, "в засаде");
    }

    @Override
    public void onAfterAttach() {
        target.log("Вы засели в засаде.");
    }

    @Override
    public void onAfterDetach() {
        target.log("Вы перестали сидеть в засаде.");
    }

    @Override
    public Collection<IEvent> battleBegins(IEvent event) {
        if (imDefender(event)) {
            return withPlayers(event, (attacker, defender) -> {
                attacker.log("Вы натыкаетесь на засаду и вынуждены защищаться!");
                defender.log("Кто-то пытается вас атаковать, но вы перехватываете инициативу!");
                final BattleEvent retaliationEvent = new BattleEvent(defender, attacker);
                retaliationEvent.setAttribute(BattleFatigueEffect.FAIR, false);
                return multipleEvents(new CancelEvent(), new EffectEvent(TYPE), retaliationEvent);
            });
        } else {
            return singleEvent(new EffectEvent(TYPE));
        }
    }

}
