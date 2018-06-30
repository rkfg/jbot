package me.rkfg.xmpp.bot.plugins.game.item.armor;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;
import me.rkfg.xmpp.bot.plugins.game.repository.ArmorRepository;

public class RepositoryArmor extends AbstractArmor {

    public RepositoryArmor(TypedAttributeMap content) {
        super(null, content.get(DEF).orElse(0), content.get(PRT).orElse(0), content.get(ArmorRepository.DESC_CNT).orElse(null));
    }

}
