package me.rkfg.xmpp.bot.plugins.game.effect;

import java.util.Collection;

import me.rkfg.xmpp.bot.plugins.game.event.BattleInviteEvent;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class BattleFatigueEffect extends AbstractEffect implements IFatigueEffect {

    public static final String TYPE = "battlefatigue";
    public static final TypedAttribute<Boolean> FAIR = TypedAttribute.of("fair");

    public BattleFatigueEffect(int stmCost) {
        super(TYPE, "устаёт в бою");
        initFatigue(stmCost);
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        return processFatigue(event, "Вы слишком устали, чтобы сражаться.",
                e -> e.isOfType(BattleInviteEvent.TYPE) && e.matchAttributeValue(FAIR, true) && e.getSource() == target);
    }

}
