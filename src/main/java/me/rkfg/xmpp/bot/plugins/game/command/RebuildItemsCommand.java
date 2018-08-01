package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Utils.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.event.SearchEvent;
import me.rkfg.xmpp.bot.plugins.game.item.IItem;
import me.rkfg.xmpp.bot.plugins.game.item.IItem.Type;
import me.rkfg.xmpp.bot.plugins.game.repository.IObjectRepository;

public class RebuildItemsCommand extends AbstractCommand {

    private static final int REBUILD_RETRIES = 10;
    private static final int ITEMS_REQUIRED = 2;

    @Override
    public String getCommand() {
        return "пересобрать";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        List<String> itemIdxs = args.collect(Collectors.toList());
        try {
            if (itemIdxs.size() < ITEMS_REQUIRED) {
                throw new NumberFormatException();
            }
            List<IItem> items = itemIdxs.stream().map(Integer::valueOf).map(i -> getBackpackItem(i, player)).collect(Collectors.toList());
            Integer resultTier = (int) (items.stream().mapToInt(this::getItemTier).average().orElse(0)) + items.size() / ITEMS_REQUIRED;
            Optional<IObjectRepository<? extends IItem>> repo = getRepo(items);
            boolean retry = true;
            int tries = REBUILD_RETRIES;
            Optional<? extends IItem> randomItem = Optional.empty();
            while (retry && tries > 0) {
                randomItem = SearchEvent.getRandomItem(repo, resultTier);
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

    private Optional<IObjectRepository<? extends IItem>> getRepo(List<IItem> items) {
        Map<IItem.Type, Integer> itemCnt = new EnumMap<>(IItem.Type.class);
        items.forEach(i -> itemCnt.put(i.getItemType(), itemCnt.getOrDefault(i.getItemType(), 0) + 1));
        int max = 0;
        int secondMax = 0;
        Type type = null;
        for (Entry<Type, Integer> e : itemCnt.entrySet()) {
            if (e.getValue() >= max) {
                secondMax = max;
                max = e.getValue();
                type = e.getKey();
            }
        }
        if (max == secondMax || type == null) {
            return SearchEvent.getRepo(0);
        }
        return SearchEvent.getRepo(type.ordinal());
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
