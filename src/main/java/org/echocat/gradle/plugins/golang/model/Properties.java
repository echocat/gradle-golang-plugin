package org.echocat.gradle.plugins.golang.model;

import groovy.lang.GroovyObjectSupport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Properties<V> extends GroovyObjectSupport {

    private final Map<String, V> _map = new HashMap<>();

    public V get(String key) {
        return _map.get(key);
    }

    public void put(String key, V value) {
        _map.put(key, value);
    }

    public void remove(String key) {
        _map.remove(key);
    }

    public Set<String> keySet() {
        return _map.keySet();
    }

    public Set<Entry<String, V>> entrySet() {
        return _map.entrySet();
    }

    public boolean containsKey(String key) {
        return _map.containsKey(key);
    }

    public boolean isEmpty() {
        return _map.isEmpty();
    }

    public int size() {
        return _map.size();
    }

    public void putAll(Map<? extends String, ? extends V> m) {
        _map.putAll(m);
    }

    public void putAll(Properties<V> m) {
        putAll(m._map);
    }

    public Object methodMissing(String name, Object args) {
        final Object[] argsArray = (Object[]) args;
        if (argsArray.length > 1) {
            throw new IllegalArgumentException("There are only one element allowed for properties. But got: " + name + ": " + Arrays.toString(argsArray));
        }
        // noinspection unchecked
        put(name, (V) argsArray[0]);
        return name;
    }

}
