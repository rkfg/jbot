package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class WeaponRepository extends AbstractContentRepository {

    @Override
    public void loadContent() {
        loadContent("weapons.txt");
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        TypedAttributeMap result = new TypedAttributeMap();
        try {
            result.put(CONTENT_ID, parts[0]);
            result.put(ATK, Integer.valueOf(parts[1]));
            result.put(DEF, Integer.valueOf(parts[2]));
            result.put(STR, Integer.valueOf(parts[3]));
            result.put(TIER_CNT, Integer.valueOf(parts[4]));
            result.put(DESC_CNT, parts[5]);
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
        return 6;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

}
