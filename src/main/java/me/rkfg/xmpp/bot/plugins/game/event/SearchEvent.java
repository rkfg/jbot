package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.CowardEffect;
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
                final Optional<ISlot> slot = item.getFittingSlot().flatMap(p::getSlot);
                Optional<IItem> slotItem = slot.flatMap(ISlot::getItem);
                final String slotName = unboxString(item.getFittingSlot().map(TypedAttribute::getAccusativeName));
                final String itemDesc = unboxString(item.getDescription(Verbosity.WITH_PARAMS));
                final String itemDescVerbose = capitalize(unboxString(item.getDescription(Verbosity.VERBOSE)));
                if (!slot.isPresent() || slotItem.isPresent()) {
                    p.log("Вы нашли %s: %s [№%d в рюкзаке]. %s", slotName, itemDesc, p.getBackpack().size() + 1, itemDescVerbose);
                    p.enqueueEvent(new ItemPickupEvent(item));
                } else {
                    p.log("Вы нашли %s: %s. %s", slotName, itemDesc, itemDescVerbose);
                    p.enqueueEquipItem(item);
                }
            });
        });
    }

    public Optional<? extends IItem> tryRandomItem(IPlayer p) {
        int avg = (p.getStat(ATK) + p.getStat(DEF) + p.getStat(LCK)) / 3;
        int searchDRN = Utils.drn();
        int search = avg + searchDRN;
        int terrDRN = Utils.drn();
        int cowardPts = p.getEffect(CowardEffect.TYPE).flatMap(e -> e.getAttribute(CowardEffect.COWARD_PTS)).orElse(0);
        int territory = terrDRN - 6 + cowardPts;
        int diff = search - territory;
        log.debug("Search started, ATK: {}, DEF: {}, LCK: {}, avg: {}, DRN: {}, search: {}", p.getStat(ATK), p.getStat(DEF), p.getStat(LCK),
                avg, searchDRN, search);
        log.debug("territory: {}, DRN: {}, coward pts: {}, diff: {}", territory, terrDRN, cowardPts, diff);
        if (diff < 1) {
            return Optional.empty();
        }
        int tier = 1;
        if (diff > 16 && diff <= 21) {
            tier = 2;
        }
        if (diff > 21 && diff <= 26) {
            tier = 3;
        }
        if (diff > 26) {
            tier = 4;
        }
        log.debug("Tier: {}", tier);
        return getRandomItem(getRepo(0), tier);
    }

    public static Optional<IObjectRepository<? extends IItem>> getRepo(int type) {
        if (type < 1) {
            type = Utils.dice("1d3");
        }
        switch (type) {
        case 1:
            return Optional.of(World.THIS.getWeaponRepository());
        case 2:
            return Optional.of(World.THIS.getArmorRepository());
        case 3:
            return Optional.of(World.THIS.getUsableRepository());
        default:
            return Optional.empty();
        }

    }

    public static Optional<? extends IItem> getRandomItem(Optional<IObjectRepository<? extends IItem>> repo, int tier) {
        Optional<? extends IItem> found = Optional.empty();
        while (tier > 0 && !found.isPresent()) {
            final int t = tier;
            found = repo.flatMap(r -> r.getRandomObjectByTier(t));
            tier--;
        }
        return found;
    }
}
