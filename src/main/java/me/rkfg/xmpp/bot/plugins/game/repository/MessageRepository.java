package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class MessageRepository extends AbstractContentRepository<String> {

    private int contentNumber = 0;

    @Override
    public void loadContent() {
        loadContent("messages.txt");
    }

    @Override
    public Optional<String> contentToObject(TypedAttributeMap content) {
        return content.get(DESC_V_CNT);
    }

    @Override
    protected Optional<TypedAttributeMap> parse(String[] parts) {
        TypedAttributeMap result = new TypedAttributeMap();
        result.put(DESC_CNT, parts[0]);
        result.put(DESC_V_CNT, parts[1]);
        result.put(CONTENT_ID, "" + contentNumber++);
        return Optional.of(result);
    }

    @Override
    protected int getMaxParts() {
        return 2;
    }

    @Override
    protected int getMinParts() {
        return 2;
    }

    @Override
    protected void doIndex(TypedAttributeMap item) {
        indexAttr(item, DESC_CNT, DESC_IDX);
    }

}
