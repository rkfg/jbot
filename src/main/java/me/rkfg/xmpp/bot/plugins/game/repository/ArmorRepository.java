package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.AbstractArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class ArmorRepository extends AbstractContentRepository<IArmor> implements IHasEffects {

    public ArmorRepository(String dataDir) {
        super(dataDir);
    }

    public class Armor extends AbstractArmor {

        public Armor(TypedAttributeMap content) {
            super(content.get(CONTENT_ID).orElse(""), content.get(DEF).orElse(0), content.get(PRT).orElse(0),
                    content.get(DESC_CNT).orElse(null));
            setObjectVerboseDescription(content, this);
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
            int effShift = 0;
            if (parts.length == 7) {
                processEffects(result, parts[4]);
                effShift += 1;
            }
            result.put(DESC_CNT, parts[4 + effShift]);
            result.put(DESC_V_CNT, parts[5 + effShift]);
            return Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    protected int getMaxParts() {
        return 7;
    }

    @Override
    protected int getMinParts() {
        return 6;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

    @Override
    public Optional<IArmor> contentToObject(TypedAttributeMap content) {
        return Optional.of(attachEffects(new Armor(content), content));
    }
}
