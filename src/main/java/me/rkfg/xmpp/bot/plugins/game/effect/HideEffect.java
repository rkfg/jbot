package me.rkfg.xmpp.bot.plugins.game.effect;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.EffectEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;

public class HideEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "hidden";

    public HideEffect() {
        super(TYPE, "скрывается");
    }

    @Override
    public Collection<IEvent> battleBegins(IEvent event) {
        if (imDefender(event)) {
            return withPlayers(event, (attacker, defender) -> {
                int hide = defender.getStat(DEF) + defender.getStat(LCK) + drn();
                int seek = attacker.getStat(ATK) + attacker.getStat(LCK) + drn();
                if (seek > hide) {
                    attacker.log("Жертва старательно скрывалась, но вы сумели её обнаружить!");
                    defender.log("О нет, вас заметили и собираются хорошенько отметелить!");
                    return unhide();
                } else {
                    attacker.log("Вам не удалось найти жертву.");
                    defender.log("Кто-то искал вас, чтобы подраться, но не нашёл.");
                    return cancelEvent();
                }
            });
        } else {
            return unhide();
        }
    }

    public Collection<IEvent> unhide() {
        return singleEvent(new EffectEvent(TYPE));
    }

    @Override
    public void onAfterAttach() {
        target.log("Вы спрятались.");
    }

    @Override
    public void onAfterDetach() {
        target.log("Вы перестали прятаться.");
    }

}
