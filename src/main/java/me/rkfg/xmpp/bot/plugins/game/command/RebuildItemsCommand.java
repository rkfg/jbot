package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Arrays.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;

public class RebuildItemsCommand implements ICommandHandler, IUsesBackpack {

    private static final int REBUILD_RETRIES = 10;

    @Override
    public Collection<String> getCommand() {
        return asList("пересобрать");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        List<String> itemIdxs = args.collect(Collectors.toList());
        try {
            if (itemIdxs.size() < 3) {
                throw new NumberFormatException();
            }
            List<IItem> items = itemIdxs.stream().map(Integer::valueOf).map(i -> getBackpackItem(i, player)).collect(Collectors.toList());
            Integer resultTier = (int) (items.stream().mapToInt(this::getItemTier).average().orElse(0)) + items.size() / 3;
            boolean retry = true;
            int tries = REBUILD_RETRIES;
            Optional<? extends IItem> randomItem = Optional.empty();
            while (retry && tries > 0) {
                randomItem = SearchEvent.getRandomItem(resultTier);
                retry = randomItem.map(i -> {
                    for (IItem item : items) {
                        if (item.getType().equals(i.getType())) {
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
                tries--;
            }
            if (!randomItem.isPresent()) {
                player.log("Вам не удалось собрать из этих предметов ничего полезного.");
            } else {
                randomItem.ifPresent(ri -> player.as(MUTABLEPLAYER_OBJ).ifPresent(p -> {
                    p.log("Вы взяли %s и собрали из них %s.",
                            items.stream().map(IItem::getItemDescription).reduce(commaReducer).orElse("<ошибка>"), ri.getItemDescription());
                    items.forEach(p::removeFromBackpack);
                    p.enqueuePickup(ri);
                }));
            }
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

    private Integer getItemTier(IItem item) {
        return item.getContentRepository().flatMap(r -> r.getContentById(item.getType())).flatMap(c -> c.get(TIER_CNT)).orElse(0);
    }

    @Override
    public Optional<String> getHelp() {
        return Optional
                .of("Пересобрать указанные предметы в рюкзаке, чтобы получить более качественный предмет (укажите не менее трёх номеров).");
    }

}
