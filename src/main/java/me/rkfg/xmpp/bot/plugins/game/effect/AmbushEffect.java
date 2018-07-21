package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;
import me.rkfg.xmpp.bot.plugins.game.event.CancelEvent;
import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class AmbushEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "ambush";

    private class AmbushStatChange extends StatsEffect {

        public static final String TYPE = "ambushstat";

        public AmbushStatChange(boolean success) {
            super(TYPE, "эффект от засады");
            if (success) {
                setStatChange(DEF, 2);
                setStatChange(ATK, 2);
            } else {
                setStatChange(DEF, -2);
                setStatChange(ATK, -2);
            }
        }
    }

    public AmbushEffect() {
        super(TYPE, "в засаде");
    }

    @Override
    public void onAfterAttach() {
        target.log("Вы засели в засаде.");
        target.enqueueDetachEffect(HideEffect.TYPE);
    }

    @Override
    public void onAfterDetach() {
        target.log("Вы перестали сидеть в засаде.");
    }

    @Override
    public Collection<IEvent> battleInvite(IEvent event) {
        if (imDefender(event)) {
            return withPlayers(event, (attacker, defender) -> {
                int ambush = defender.getStat(ATK) + defender.getStat(LCK) + drn();
                int vigilance = attacker.getStat(DEF) + attacker.getStat(LCK) + drn();
                if (ambush > vigilance) {
                    attacker.log("Вы натыкаетесь на засаду и вынуждены защищаться!");
                    defender.log("Кто-то пытается вас атаковать, но вы перехватываете инициативу!");
                    final BattleEvent retaliationEvent = new BattleEvent(defender, attacker);
                    retaliationEvent.setAttribute(BattleFatigueEffect.FAIR, false);
                    defender.enqueueAttachEffect(new AmbushStatChange(true));
                    return multipleEvents(new CancelEvent(), retaliationEvent);
                } else {
                    attacker.log("Вы заблаговременно замечаете засаду, заходите сзади и атакуете жертву.");
                    defender.log("Ваша засада была обнаружена, вы в панике!");
                    defender.enqueueAttachEffect(new AmbushStatChange(false));
                    return noEvent();
                }
            });
        }
        return noEvent();
    }

    @Override
    public Collection<IEvent> battleEnds(IEvent event) {
        return multipleEvents(new EffectEvent(AmbushStatChange.TYPE), new EffectEvent(TYPE));
    }

}
