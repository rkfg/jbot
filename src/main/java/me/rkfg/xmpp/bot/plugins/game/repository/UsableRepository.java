package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.AbstractItem;
import me.rkfg.xmpp.bot.plugins.game.item.IUsable;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class UsableRepository extends AbstractContentRepository<IUsable> implements IHasEffects {

    public static class Usable extends AbstractItem implements IUsable {

        public Usable(TypedAttributeMap content) {
            super(content.get(CONTENT_ID).orElse(""), content.get(DESC_CNT).orElse(null));
            content.get(USE_CNT).ifPresent(c -> setAttribute(USE_CNT, c));
        }
    }

    @Override
    public void loadContent() {
        loadContent("usable.txt");
    }

    @Override
    public Optional<IUsable> contentToObject(TypedAttributeMap content) {
        return Optional.of(attachEffects(new Usable(content), content));
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        try {
            TypedAttributeMap result = new TypedAttributeMap();
            result.put(CONTENT_ID, parts[0]);
            result.put(TIER_CNT, Integer.valueOf(parts[1]));
            result.put(USE_CNT, Integer.valueOf(parts[2]));
            int effShift = 0;
            if (parts.length > 5) {
                processEffects(result, parts[3]);
                effShift += 1;
            }
            result.put(DESC_CNT, parts[3 + effShift]);
            result.put(DESC_V_CNT, parts[4 + effShift]);
            return Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    protected int getMaxParts() {
        return 6;
    }

    @Override
    protected int getMinParts() {
        return 5;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

}
