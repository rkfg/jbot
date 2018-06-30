package me.rkfg.xmpp.bot.plugins.game.item.weapon;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;
import me.rkfg.xmpp.bot.plugins.game.repository.WeaponRepository;

public class RepositoryWeapon extends AbstractWeapon {

    public RepositoryWeapon(TypedAttributeMap content) {
        super(null, content.get(ATK).orElse(0), content.get(DEF).orElse(0), content.get(STR).orElse(0),
                content.get(WeaponRepository.DESC_CNT).orElse(null));
    }
}
