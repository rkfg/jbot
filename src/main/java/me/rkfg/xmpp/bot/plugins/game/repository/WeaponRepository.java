package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.item.AbstractWeapon;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class WeaponRepository extends AbstractContentRepository<IWeapon> implements IHasEffects {

    public WeaponRepository(String dataDir) {
        super(dataDir);
    }

    public class Weapon extends AbstractWeapon {

        public Weapon(TypedAttributeMap content) {
            super(content.get(CONTENT_ID).orElse(""), content.get(ATK).orElse(0), content.get(DEF).orElse(0), content.get(STR).orElse(0),
                    content.get(DESC_CNT).orElse(null));
            setObjectVerboseDescription(content, this);
        }
    }

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
            int effShift = 0;
            if (parts.length == 8) {
                processEffects(result, parts[5]);
                effShift += 1;
            }
            result.put(DESC_CNT, parts[5 + effShift]);
            result.put(DESC_V_CNT, parts[6 + effShift]);
            return Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    protected int getMaxParts() {
        return 8;
    }

    @Override
    protected int getMinParts() {
        return 7;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

    @Override
    public Optional<IWeapon> contentToObject(TypedAttributeMap content) {
        return Optional.of(attachEffects(new Weapon(content), content));
    }
}
