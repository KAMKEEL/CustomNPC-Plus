package kamkeel.npcs.util;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.util.*;
import java.util.function.Supplier;

public class Register<T> {
    public static final Map<String, List<String>> REGISTERED_NAMESPACES = new LinkedHashMap<>();
    public static final Map<String, String> NAMESPACE_DISPLAY_NAMES = new LinkedHashMap<>();
    protected final String registryKey;
    protected final String namespace;
    protected final Map<String, Supplier<T>> entries = new LinkedHashMap<>();

    private Register(String registryKey, String namespace) {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    public T register(String factoryName, Supplier<T> factory) {
        entries.put(registryKey + "." + namespace + "." + factoryName, factory);
        return factory.get();
    }

    public static boolean isEmpty(String registryKey) {
        if (REGISTERED_NAMESPACES.isEmpty()) return true;

        List<String> list = REGISTERED_NAMESPACES.get(registryKey);
        if (list == null || list.isEmpty()) return true;

        return false;
    }

    public static class Abilities extends Register<Ability> {
        protected final Map<String, String> uniqueNames = new LinkedHashMap<>();

        private Abilities(String namespace) {
            super("ability", namespace);
        }

        @Override
        public Ability register(String factoryName, Supplier<Ability> factory) {
            String name = registryKey + "." + namespace + "." + factoryName.trim().toLowerCase().replaceAll(" ", "_");
            entries.put(name, factory);
            uniqueNames.put(name, factoryName);
            return factory.get();
        }

        public void register() {
            for (Map.Entry<String, Supplier<Ability>> entry : entries.entrySet()) {
                AbilityController.Instance.registerAbility(uniqueNames.get(entry.getKey()), entry.getValue().get());
            }
        }

        public static Register.Abilities create(String namespace, String displayName) {
            if (!REGISTERED_NAMESPACES.containsKey("ability"))
                REGISTERED_NAMESPACES.put("ability", new ArrayList<>());

            if (REGISTERED_NAMESPACES.get("ability").contains(namespace)) {
                LogWriter.error("REGISTER ABILITIES: Namespace " + namespace + " already registered!");
            }

            REGISTERED_NAMESPACES.get("ability").add(namespace);
            NAMESPACE_DISPLAY_NAMES.put(namespace, displayName);

            return new Register.Abilities(namespace);
        }
    }

    public static class Animations extends Register<Animation> {
        private final Class<?> modClass;
        private final String animationsPath;

        private Animations(Class<?> modClass, String animationsPath, String namespace) {
            super("animation", namespace);
            this.animationsPath = animationsPath;
            this.modClass = modClass;
        }

        @Override
        public Animation register(String factoryName, Supplier<Animation> factory) {
            return super.register(factoryName, factory);
        }

        public void register() {
            try {
                String path = "/assets/" + namespace + "/" + animationsPath;
                for (Map.Entry<String, Supplier<Animation>> entry : entries.entrySet()) {
                    String name = entry.getKey().substring(entry.getKey().indexOf(namespace + ".") + 1);
                    AnimationController.Instance.loadBuiltInAnimation(modClass, path, name);
                }
            } catch (Exception e) {
                LogWriter.error("Error scanning built-in animations folder", e);
            }
        }

        public static Register.Animations create(Class<?> modClass, String animationsPath, String namespace) {
            if (!REGISTERED_NAMESPACES.containsKey("animation"))
                REGISTERED_NAMESPACES.put("animation", new ArrayList<>());

            if (REGISTERED_NAMESPACES.get("animation").contains(namespace)) {
                LogWriter.error("REGISTER ANIMATIONS: Namespace " + namespace + " already registered!");
            }

            REGISTERED_NAMESPACES.get("animation").add(namespace);

            return new Register.Animations(modClass, animationsPath, namespace);
        }
    }
}
