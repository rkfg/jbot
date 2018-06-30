package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class RudeDrawEffect extends AbstractBattleEffect {

    public static final String TYPE = "rudedraw";

    public RudeDrawEffect() {
        super(TYPE, "иногда разрисовывает соперника");
    }

    @Override
    protected Collection<IEvent> attackSuccess(IEvent event) {
        if (Utils.drn() > 5) {
            event.getSource().log("Вы ухитряетесь разрисовать соперника деморализующими нехорошими словами и рисунками");
            event.getTarget().log("Соперник рисует на вас нехорошие слова и рисунки, вы деморализованы");
            event.getTarget().as(PLAYER_OBJ).ifPresent(p -> p.enqueueAttachEffect(new RudeDrawingsEffect()));
        }
        return super.attackSuccess(event);
    }
}
