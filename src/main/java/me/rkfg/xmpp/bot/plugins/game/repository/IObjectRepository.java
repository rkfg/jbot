package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;
import me.rkfg.xmpp.bot.plugins.game.misc.IHasDescription.Verbosity;
import me.rkfg.xmpp.bot.plugins.game.repository.AbstractContentRepository.IndexPointer;

public interface IObjectRepository<O> extends IContentRepository {

    default Optional<O> getObjectById(String id) {
        return getContentById(id).flatMap(this::contentToObject);
    }

    default Optional<O> getRandomObject() {
        return getRandomContent().flatMap(this::contentToObject);
    }

    default <T> Optional<O> getRandomObject(IndexPointer<T> indexPtr, T value) {
        return getRandomContent(indexPtr, value).flatMap(this::contentToObject);
    }

    default Optional<O> getRandomObjectByTier(int tier) {
        return getRandomObject(TIER_IDX, tier);
    }

    Optional<O> contentToObject(TypedAttributeMap content);

    default void setObjectVerboseDescription(TypedAttributeMap content, IHasDescription obj) {
        content.get(DESC_V_CNT).ifPresent(d -> obj.setDescription(Verbosity.VERBOSE, d));
    }

}
