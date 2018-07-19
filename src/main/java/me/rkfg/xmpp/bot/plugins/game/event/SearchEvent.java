package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.ISlot;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;
import me.rkfg.xmpp.bot.plugins.game.repository.IObjectRepository;

public class SearchEvent extends AbstractEvent {

    public static final String TYPE = "searchevent";

    public SearchEvent() {
        super(TYPE);
    }

    @Override
    public void apply() {
        getTarget().as(PLAYER_OBJ).ifPresent(p -> {
            Optional<? extends IItem> result = tryRandomItem(p);
            if (!result.isPresent()) {
                p.log("Вы не смогли обнаружить ничего полезного.");
                return;
            }
            result.ifPresent(item -> {
                getTarget().log(String.format("Вы нашли %s: %s", unboxString(item.getFittingSlot().map(TypedAttribute::getAccusativeName)),
                        unboxString(item.getDescription())));
                final Optional<ISlot> slot = item.getFittingSlot().flatMap(p::getSlot);
                Optional<IItem> slotItem = slot.flatMap(ISlot::getItem);
                if (!slot.isPresent() || slotItem.isPresent()) {
                    p.enqueueEvent(new ItemPickupEvent(item));
                } else {
                    p.enqueueEquipItem(item);
                }
            });
        });
    }

    public Optional<? extends IItem> tryRandomItem(IPlayer p) {
        int search = p.getStat(ATK) + p.getStat(DEF) + p.getStat(STM) + p.getStat(LCK) + Utils.drn();
        int territory = 30 + Utils.drn();
        int diff = search - territory;
        if (diff < 1) {
            return Optional.empty();
        }
        int tier = 1;
        if (diff > 10 && diff < 16) {
            tier = 2;
        }
        if (diff > 15 && diff < 21) {
            tier = 3;
        }
        if (diff > 20) {
            tier = 4;
        }
        return getRandomItem(tier);
    }

    public static Optional<? extends IItem> getRandomItem(int tier) {
        Optional<? extends IItem> found = Optional.empty();
        while (tier > 0 && !found.isPresent()) {
            int type = Utils.dice("1d3");
            Optional<IObjectRepository<? extends IItem>> repo;
            switch (type) {
            case 1:
                repo = Optional.of(World.THIS.getWeaponRepository());
                break;
            case 2:
                repo = Optional.of(World.THIS.getArmorRepository());
                break;
            case 3:
                repo = Optional.of(World.THIS.getUsableRepository());
                break;
            default:
                return Optional.empty();
            }
            final int t = tier;
            found = repo.flatMap(r -> r.getRandomObjectByTier(t));
            tier--;
        }
        return found;
    }
}
