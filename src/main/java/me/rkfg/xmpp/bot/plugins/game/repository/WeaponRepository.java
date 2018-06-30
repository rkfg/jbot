package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.item.weapon.AbstractWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class WeaponRepository extends AbstractContentRepository<IWeapon> {

    private EffectRepository effectRepository;

    public static final TypedAttribute<Set<IEffect>> EFFECTS = TypedAttribute.of("effects");

    public class Weapon extends AbstractWeapon {

        public Weapon(TypedAttributeMap content) {
            super(null, content.get(ATK).orElse(0), content.get(DEF).orElse(0), content.get(STR).orElse(0),
                    content.get(WeaponRepository.DESC_CNT).orElse(null));
        }
    }

    public WeaponRepository(EffectRepository effectRepository) {
        this.effectRepository = effectRepository;
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
            if (parts.length == 7) {
                result.put(DESC_CNT, parts[6]);
                result.put(EFFECTS, Stream.of(parts[5].split(",")).map(String::trim).map(type -> effectRepository.getObjectById(type))
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet()));
            } else {
                result.put(DESC_CNT, parts[5]);
            }
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
    protected Optional<IWeapon> contentToObject(TypedAttributeMap content) {
        final Weapon weapon = new Weapon(content);
        content.get(EFFECTS).ifPresent(fx -> fx.forEach(weapon::attachEffect));
        return Optional.of(weapon);
    }
}
