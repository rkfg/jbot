package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.BleedEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.DeadEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.NoGuardSleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.SleepEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.CursedEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.ItemStatEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.RudeDrawEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.item.RudeDrawingsEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class EffectRepository extends AbstractContentRepository<IEffect> {

    public static final TypedAttribute<Class<? extends IEffect>> EFFECT_CNT = TypedAttribute.of("effectcnt");

    @Override
    public void loadContent() {
        addEffect(BleedEffect.class);
        addEffect(CursedEffect.class);
        addEffect(DeadEffect.class);
        addEffect(ItemStatEffect.class);
        addEffect(NoGuardSleepEffect.class);
        addEffect(SleepEffect.class);
        addEffect(StatsEffect.class);
        addEffect(RudeDrawEffect.class);
        addEffect(RudeDrawingsEffect.class);

    }

    private void addEffect(Class<? extends IEffect> clazz) {
        TypedAttributeMap effectDesc = new TypedAttributeMap();
        String type;
        try {
            type = clazz.newInstance().getType();
            effectDesc.put(CONTENT_ID, type);
            effectDesc.put(EFFECT_CNT, clazz);
            content.put(type, effectDesc);
        } catch (InstantiationException | IllegalAccessException e) {
            log.warn("Couldn't add effect of class {}: {}", clazz.getName(), e);
        }
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        return Optional.empty();
    }

    @Override
    protected int getMaxParts() {
        return 0;
    }

    @Override
    protected int getMinParts() {
        return 0;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        // doesn't require indexing yet
    }

    @Override
    public Optional<IEffect> contentToObject(TypedAttributeMap content) {
        return content.get(EFFECT_CNT).map(t -> {
            try {
                return t.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Couldn't instantiate {}: {}", t.getName(), e);
                return null;
            }
        });
    }

}
