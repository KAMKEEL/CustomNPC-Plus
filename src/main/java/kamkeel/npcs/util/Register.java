package kamkeel.npcs.util;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Register<T> {
    private final String registryKey;
    protected final String namespace;
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

    public static class Animations extends Register<Animation> {
        private final Class<?> modClass;
        private final String animationsPath;

        public Animations(Class<?> modClass, String namespace, String animationsPath) {
            super("animation", namespace);
            this.animationsPath = animationsPath;
            this.modClass = modClass;
        }

        public void register() throws Exception {
            try {

            } catch(Exception e) {
                String path = "/assets/" + namespace + "/" + animationsPath + "/";
                for (Map.Entry<String, Supplier<Animation>> entry : entries.entrySet()) {
                    String key = entry.getKey().substring(entry.getKey().indexOf(":"));
                    AnimationController.Instance.loadBuiltInAnimation(modClass, path, key);
                }
            }
        }
    }
}
