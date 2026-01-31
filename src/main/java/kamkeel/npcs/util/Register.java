package kamkeel.npcs.util;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Register<T> {
    private final String registryKey;
    private final String namespace;
    protected final Map<String, Supplier<T>> entries = new LinkedHashMap<>();

    public Register(String registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public T register(String factoryName, Supplier<T> factory) {
        entries.put(registryKey + "." + namespace + ":" + factoryName, factory);
        return factory.get();
    }

    public static class Abilities extends Register<Ability> {
        public Abilities(String namespace) {
            super("ability", namespace);
        }

        public void register() {
            for (Map.Entry<String, Supplier<Ability>> entry : entries.entrySet()) {
                AbilityController.Instance.registerType(entry.getKey(), entry.getValue());
            }
        }
    }
}
