package kamkeel.npcs.util;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.IEffectAction;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
        protected final Map<String, Map<String, Supplier<AbilityVariant>>> variantEntries = new LinkedHashMap<>();
        protected final Map<String, String> uniqueNames = new LinkedHashMap<>();
        protected final Set<String> typeOnly = new HashSet<>();

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

        /**
         * Register a type only (shell template, no built-in preset).
         * The type will appear in the ability type picker but won't be
         * available as a pre-configured ability by name.
         */
        public Ability registerType(String factoryName, Supplier<Ability> factory) {
            String name = registryKey + "." + namespace + "." + factoryName.trim().toLowerCase().replaceAll(" ", "_");
            entries.put(name, factory);
            uniqueNames.put(name, factoryName);
            typeOnly.add(name);
            return factory.get();
        }

        public AbilityVariant registerVariant(String typeId, String variantName, String group, Consumer<Ability> configurator) {
            return registerVariant(typeId, variantName, () -> new AbilityVariant(variantName, group, configurator));
        }

        public AbilityVariant registerVariant(String typeId, String factoryName, Supplier<AbilityVariant> factory) {
            if (!variantEntries.containsKey(typeId)) {
                variantEntries.put(typeId, new HashMap<>());
            }

            Map<String, Supplier<AbilityVariant>> type = variantEntries.get(typeId);

            String name = registryKey + "." + namespace + "." + factoryName.trim().toLowerCase().replaceAll(" ", "_");
            type.put(name, factory);
            return factory.get();
        }

        public void register() {
            for (Map.Entry<String, Supplier<Ability>> entry : entries.entrySet()) {
                AbilityController.Instance.registerType(entry.getKey(), entry.getValue());
                if (!typeOnly.contains(entry.getKey())) {
                    AbilityController.Instance.registerAbility(uniqueNames.get(entry.getKey()), entry.getValue().get());
                }
            }

            for (Map.Entry<String, Map<String, Supplier<AbilityVariant>>> entry : variantEntries.entrySet()) {
                String typeId = entry.getKey();
                Map<String, Supplier<AbilityVariant>> map = entry.getValue();
                for (Map.Entry<String, Supplier<AbilityVariant>> innerEntry : map.entrySet()) {
                    AbilityController.Instance.registerVariant(typeId, innerEntry.getValue().get());
                }
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

        public Animation[] registerBundle(String factoryName, Supplier<Animation> factory, String... appendixes) {
            List<Animation> animations = new ArrayList<>();
            for (String appendix : appendixes) {
                entries.put(registryKey + "." + namespace + "." + factoryName + "_" + appendix, factory);
                animations.add(factory.get());
            }

            return animations.toArray(new Animation[0]);
        }

        public void register() {
            try {
                String path = "/assets/" + namespace + "/" + animationsPath;
                for (Map.Entry<String, Supplier<Animation>> entry : entries.entrySet()) {
                    String prefix = registryKey + "." + namespace + ".";
                    String key = entry.getKey();
                    String name = key.substring(prefix.length());
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

    public static class EffectActions extends Register<IEffectAction> {

        private EffectActions(String namespace) {
            super("effect_action", namespace);
        }

        public IEffectAction register(String name, IEffectAction action) {
            String key = namespace + ":" + name.trim().toLowerCase().replaceAll(" ", "_");
            entries.put(key, () -> action);
            return action;
        }

        public void register() {
            for (Map.Entry<String, Supplier<IEffectAction>> entry : entries.entrySet()) {
                AbilityController.Instance.registerEffectAction(entry.getValue().get());
            }
        }

        public static Register.EffectActions create(String namespace, String displayName) {
            if (!REGISTERED_NAMESPACES.containsKey("effect_action"))
                REGISTERED_NAMESPACES.put("effect_action", new ArrayList<>());

            if (REGISTERED_NAMESPACES.get("effect_action").contains(namespace)) {
                LogWriter.error("REGISTER EFFECT ACTIONS: Namespace " + namespace + " already registered!");
            }

            REGISTERED_NAMESPACES.get("effect_action").add(namespace);
            NAMESPACE_DISPLAY_NAMES.put(namespace, displayName);

            return new Register.EffectActions(namespace);
        }
    }
}
