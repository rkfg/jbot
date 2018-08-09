package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class ItemDropEffect extends AbstractEffect implements IUseEffect {

    public static final String TYPE = "itemdrop";

    public ItemDropEffect() {
        super(TYPE, "выдаёт случайный предмет всем игрокам");
    }

    @Override
    public Collection<IEvent> applyEffect(IGameObject target) {
        getIntParameter(0).ifPresent(ridx -> SearchEvent.getRepo(ridx).ifPresent(r -> getIntParameter(1).ifPresent(t -> {
            Optional<TypedAttributeMap> content = Optional.empty();
            while (!content.isPresent() && t > 0) {
                content = r.getRandomContent(TIER_IDX, t);
                t--;
            }
            content.ifPresent(c -> {
                for (IPlayer player : World.THIS.listPlayers()) {
                    player.log("Вы слышите громкий хлопок, и к вашим ногам падает какой-то предмет...");
                    r.contentToObject(c).ifPresent(player::enqueuePickup);
                }
            });
        })));
        return noEvent();
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
