package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.armor.AbstractArmor;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class ArmorRepository extends AbstractContentRepository<IArmor> {

    public class Armor extends AbstractArmor {

        public Armor(TypedAttributeMap content) {
            super(null, content.get(DEF).orElse(0), content.get(PRT).orElse(0), content.get(ArmorRepository.DESC_CNT).orElse(null));
        }

    }

    @Override
    public void loadContent() {
        loadContent("armor.txt");
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        TypedAttributeMap result = new TypedAttributeMap();
        try {
            result.put(CONTENT_ID, parts[0]);
            result.put(DEF, Integer.valueOf(parts[1]));
            result.put(PRT, Integer.valueOf(parts[2]));
            result.put(TIER_CNT, Integer.valueOf(parts[3]));
            result.put(DESC_CNT, parts[4]);
            return Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    protected int getMaxParts() {
        return 5;
    }

    @Override
    protected int getMinParts() {
        return 5;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

    @Override
    protected Optional<IArmor> contentToObject(TypedAttributeMap content) {
        return Optional.of(new Armor(content));
    }
}
