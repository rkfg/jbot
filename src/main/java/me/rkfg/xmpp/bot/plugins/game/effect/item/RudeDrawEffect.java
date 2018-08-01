package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IBattleEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.ITemporaryEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class RudeDrawEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "rudedraw";

    private class RudeDrawingsEffect extends StatsEffect implements ITemporaryEffect {

        private static final String TYPE = "rudedrawingseffect";

        public RudeDrawingsEffect() {
            super(TYPE, "изрисован непристойностями");
            setStatChange(DEF, -2);
            initTemporary(5);
        }

        @Override
        public Collection<IEvent> processEvent(IEvent event) {
            return processTemporary(event, TYPE, "Нехорошие рисунки на вас стёрлись.");
        }

    }

    public RudeDrawEffect() {
        super(TYPE, "иногда разрисовывает соперника");
    }

    @Override
    public Collection<IEvent> attackSuccess(IEvent event) {
        if (Utils.drn() > 13) {
            return withPlayers(event, (attacker, defender) -> {
                attacker.log("Вы ухитряетесь разрисовать соперника деморализующими нехорошими словами и рисунками");
                defender.log("Соперник рисует на вас нехорошие слова и рисунки, вы деморализованы");
                defender.enqueueAttachEffect(new RudeDrawingsEffect());
                return noEvent();
            });
        }
        return noEvent();
    }
}
