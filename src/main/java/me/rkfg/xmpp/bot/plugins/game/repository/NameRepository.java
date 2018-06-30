package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class NameRepository extends AbstractContentRepository<String> {

    private int counter = 1;

    @Override
    protected int getMaxParts() {
        return 2;
    }

    @Override
    protected int getMinParts() {
        return 2;
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        TypedAttributeMap result = new TypedAttributeMap();
        result.put(CONTENT_ID, String.valueOf(counter++));
        result.put(TIER_CNT, Integer.valueOf(parts[0]));
        result.put(DESC_CNT, parts[1]);
        return Optional.of(result);
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, TIER_CNT, TIER_IDX);
    }

    @Override
    public void loadContent() {
        loadContent("names.txt");
    }

    @Override
    protected Optional<String> contentToObject(TypedAttributeMap content) {
        return content.get(DESC_CNT);
    }

    @Override
    public Optional<String> getObjectById(String id) {
        return getContentById(id).flatMap(c -> c.get(DESC_CNT));
    }

    @Override
    public Optional<String> getRandomObject() {
        return getRandomContent().flatMap(c -> c.get(DESC_CNT));
    }

}
