package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public abstract class AbstractContentRepository<O> implements IContentRepository, IObjectRepository<O> {

    protected Logger log = LoggerFactory.getLogger(getClass());
    private Random rnd = new SecureRandom();

    // unique id => content map
    protected Map<String, TypedAttributeMap> content = new HashMap<>();

    public static class IndexPointer<T> extends TypedAttribute<Map<T, Set<String>>> {

        protected IndexPointer(String name) {
            super(name);
        }

        public static <T> IndexPointer<T> named(String name) {
            return new IndexPointer<>(name);
        }

    }

    // IndexPointer => (T => (set of conforming unique ids))
    protected TypedAttributeMap index = new TypedAttributeMap();
    private String dataDir;

    public AbstractContentRepository(String dataDir) {
        this.dataDir = dataDir;
    }
    
    protected void loadContent(String filename) {
        int maxParts = getMaxParts();
        int minParts = getMinParts();
        try (BufferedReader br = new BufferedReader(new FileReader(dataDir + File.separatorChar + filename))) {
            br.lines().filter(line -> !line.startsWith("#")).forEach(line -> {
                String[] parts = line.split("\\|", maxParts);
                if (parts.length >= minParts) {
                    Optional<TypedAttributeMap> item = parse(parts);
                    item.ifPresent(i -> i.get(CONTENT_ID).ifPresent(id -> {
                        content.put(id, i);
                        doIndex(i);
                    }));
                }
            });
        } catch (FileNotFoundException e) {
            log.warn("Файл {} не найден.", filename);
        } catch (IOException e) {
            log.warn("Ошибка ввода-вывода при чтении файла {}: {}", filename, e);
        }
    }

    @Override
    public Collection<TypedAttributeMap> getAllContent() {
        return new HashSet<>(content.values());
    }

    @Override
    public <T> Collection<TypedAttributeMap> getContent(IndexPointer<T> indexPtr, T value) {
        return index.get(indexPtr).map(m -> m.get(value))
                .map(cntIds -> cntIds.stream().map(name -> content.get(name)).collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public <T> Optional<TypedAttributeMap> getRandomContent(IndexPointer<T> indexPtr, T value) {
        Collection<TypedAttributeMap> filteredContent = getContent(indexPtr, value);
        if (filteredContent.isEmpty()) {
            return Optional.empty();
        }
        return filteredContent.stream().skip(rnd.nextInt(filteredContent.size())).findFirst();
    }

    @Override
    public Optional<TypedAttributeMap> getRandomContent() {
        if (content.isEmpty()) {
            return Optional.empty();
        }
        return content.values().stream().skip(rnd.nextInt(content.size())).findFirst();
    }

    protected <T> void indexAttr(TypedAttributeMap item, TypedAttribute<T> itemAttr, IndexPointer<T> indexPtr) {
        item.get(CONTENT_ID).ifPresent(cntid -> index.get(indexPtr, k -> new HashMap<>())
                .ifPresent(map -> item.get(itemAttr).ifPresent(cnt -> map.computeIfAbsent(cnt, k -> new HashSet<>()).add(cntid))));
    }

    @Override
    public Optional<TypedAttributeMap> getContentById(String id) {
        return Optional.ofNullable(content.get(id));
    }

    protected abstract Optional<TypedAttributeMap> parse(String[] parts);

    protected abstract int getMaxParts();

    protected abstract int getMinParts();

    protected abstract void doIndex(TypedAttributeMap item);
}
