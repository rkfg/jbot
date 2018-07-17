package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.StatsEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class TraitsRepository extends AbstractContentRepository<IEffect> implements IHasEffects {

    @Override
    public void loadContent() {
        loadContent("traits.txt");
    }

    @Override
    public Optional<IEffect> contentToObject(TypedAttributeMap content) {
        StatsEffect result = new StatsEffect(content.get(CONTENT_ID).orElse(""), content.get(DESC_CNT).orElse(""));
        setObjectVerboseDescription(content, result);
        content.get(EFFECTS).ifPresent(
                fx -> fx.stream().map(this::instantiateEffect).filter(Optional::isPresent).map(Optional::get).forEach(result::addEffect));
        return Optional.of(result);
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        TypedAttributeMap result = new TypedAttributeMap();
        result.put(CONTENT_ID, parts[0]);
        result.put(GROUP_ID, parts[1]);
        processEffects(result, parts[2]);
        result.put(DESC_CNT, parts[3]);
        result.put(DESC_V_CNT, parts[4]);
        return Optional.of(result);
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
        indexAttr(item, GROUP_ID, GROUP_IDX);
    }

}
