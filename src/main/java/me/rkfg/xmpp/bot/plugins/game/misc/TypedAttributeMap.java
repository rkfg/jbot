package me.rkfg.xmpp.bot.plugins.game.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class TypedAttributeMap {

    Map<String, Object> map = new HashMap<>();

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public <T> boolean containsAttr(TypedAttribute<T> key) {
        return map.containsKey(key.getName());
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(TypedAttribute<T> key) {
        return Optional.ofNullable((T) map.get(key.getName()));
    }

    public <T> Optional<T> get(TypedAttribute<T> key, Function<? super TypedAttribute<T>, ? extends T> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        Optional<T> v = get(key);
        if (!v.isPresent()) {
            T newValue = mappingFunction.apply(key);
            put(key, newValue);
            return Optional.of(newValue);
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    public <T> T put(TypedAttribute<T> key, T value) {
        return (T) map.put(key.getName(), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(TypedAttribute<T> key) {
        return (T) map.remove(key.getName());
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public void clear() {
        map.clear();
    }
}
