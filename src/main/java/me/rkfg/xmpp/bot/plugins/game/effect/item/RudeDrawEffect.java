package me.rkfg.xmpp.bot.plugins.game.effect.item;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class RudeDrawEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "rudedraw";

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
