package me.rkfg.xmpp.bot.plugins.game.repository;

import java.util.Collection;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameBase;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;
import me.rkfg.xmpp.bot.plugins.game.repository.AbstractContentRepository.IndexPointer;

public interface IContentRepository extends IGameBase {
    
    void loadContent();

    <T> Collection<TypedAttributeMap> getContent(IndexPointer<T> indexPtr, T value);

    Collection<TypedAttributeMap> getAllContent();

    <T> Optional<TypedAttributeMap> getRandomContent(IndexPointer<T> indexPtr, T value);

    Optional<TypedAttributeMap> getRandomContent();
}
