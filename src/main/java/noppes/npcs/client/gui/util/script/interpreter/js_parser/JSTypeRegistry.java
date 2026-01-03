package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.*;

/**
 * Central registry for all TypeScript types parsed from .d.ts files.
 * Also manages hook function signatures and type aliases.
 */
public class JSTypeRegistry {
    
    private static JSTypeRegistry INSTANCE;
    
    // Resource location for the API definitions
    private static final String API_RESOURCE_PATH = "customnpcs:api/";
    
    // All registered types by full name (e.g., "IPlayerEvent.InteractEvent")
    private final Map<String, JSTypeInfo> types = new LinkedHashMap<>();
    
    // Type aliases (simple name -> full type name)
    private final Map<String, String> typeAliases = new HashMap<>();
    
    // Hook function signatures: functionName -> list of (paramName, paramType) pairs
    // Multiple entries for overloaded hooks
    private final Map<String, List<HookSignature>> hooks = new LinkedHashMap<>();
    
    // Primitive types
    private static final Set<String> PRIMITIVES = new HashSet<>(Arrays.asList(
        "number", "string", "boolean", "void", "any", "null", "undefined", "never", "object"
    ));
    
    private boolean initialized = false;
    private boolean initializationAttempted = false;
    
    public static JSTypeRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JSTypeRegistry();
        }
        return INSTANCE;
    }
    
    private JSTypeRegistry() {}
    
    /**
     * Initialize the registry from the embedded resources.
     * This loads .d.ts files from assets/customnpcs/api/
     */
    public void initializeFromResources() {
        if (initialized || initializationAttempted) return;
        initializationAttempted = true;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);
            
            // Load hooks.d.ts first (defines function parameter types)
            loadResourceFile(parser, "hooks.d.ts");
            
            // Load index.d.ts (type aliases)
            loadResourceFile(parser, "index.d.ts");
            
            // Load main API files
            loadResourceDirectory(parser, "noppes/npcs/api/");
            loadResourceDirectory(parser, "noppes/npcs/api/entity/");
            loadResourceDirectory(parser, "noppes/npcs/api/event/");
            loadResourceDirectory(parser, "noppes/npcs/api/handler/");
            loadResourceDirectory(parser, "noppes/npcs/api/item/");
            loadResourceDirectory(parser, "noppes/npcs/api/block/");
            loadResourceDirectory(parser, "noppes/npcs/api/gui/");
            
            resolveInheritance();
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks from resources");
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Failed to load type definitions from resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load a specific .d.ts file from resources.
     */
    private void loadResourceFile(TypeScriptDefinitionParser parser, String fileName) {
        try {
            ResourceLocation loc = new ResourceLocation("customnpcs", "api/" + fileName);
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    parser.parseDefinitionFile(sb.toString(), fileName);
                }
            }
        } catch (Exception e) {
            // File might not exist, that's ok
            System.out.println("[JSTypeRegistry] Could not load " + fileName + ": " + e.getMessage());
        }
    }
    
    /**
     * Load all .d.ts files from a resource directory.
     */
    private void loadResourceDirectory(TypeScriptDefinitionParser parser, String dirPath) {
        // In Minecraft resources, we can't list directory contents directly
        // So we'll try to load known files from a manifest or just try common patterns
        // For now, let's try loading specific known files
        String[] knownFiles = getKnownFilesForDirectory(dirPath);
        for (String file : knownFiles) {
            loadResourceFile(parser, dirPath + file);
        }
    }
    
    /**
     * Get list of known .d.ts files for a directory.
     * This is a workaround since we can't list resource directories.
     */
    private String[] getKnownFilesForDirectory(String dirPath) {
        // These are the known API files based on the actual file structure
        if (dirPath.endsWith("noppes/npcs/api/")) {
            return new String[]{
                "AbstractNpcAPI.d.ts", "IBlock.d.ts", "ICommand.d.ts", "IContainer.d.ts",
                "IDamageSource.d.ts", "INbt.d.ts", "IParticle.d.ts", "IPixelmonPlayerData.d.ts",
                "IPos.d.ts", "IScreenSize.d.ts", "ISkinOverlay.d.ts", "ITileEntity.d.ts", 
                "ITimers.d.ts", "IWorld.d.ts"
            };
        } else if (dirPath.endsWith("entity/")) {
            return new String[]{
                "ICustomNpc.d.ts", "IEntity.d.ts", "IEntityItem.d.ts", "IEntityLiving.d.ts",
                "IEntityLivingBase.d.ts", "IPlayer.d.ts", "IProjectile.d.ts", "IAnimal.d.ts",
                "IAnimatable.d.ts", "IArrow.d.ts", "IDBCPlayer.d.ts", "IFishHook.d.ts",
                "IMonster.d.ts", "IPixelmon.d.ts", "IThrowable.d.ts", "IVillager.d.ts"
            };
        } else if (dirPath.endsWith("event/")) {
            return new String[]{
                "INpcEvent.d.ts", "IPlayerEvent.d.ts", "IItemEvent.d.ts", "IBlockEvent.d.ts",
                "IDialogEvent.d.ts", "IQuestEvent.d.ts", "ICustomGuiEvent.d.ts",
                "IAnimationEvent.d.ts", "ICustomNPCsEvent.d.ts", "IFactionEvent.d.ts",
                "IForgeEvent.d.ts", "ILinkedItemEvent.d.ts", "IPartyEvent.d.ts",
                "IProjectileEvent.d.ts", "IRecipeEvent.d.ts"
            };
        } else if (dirPath.endsWith("handler/")) {
            return new String[]{
                "ICloneHandler.d.ts", "IDialogHandler.d.ts", "IFactionHandler.d.ts",
                "IQuestHandler.d.ts", "IRecipeHandler.d.ts", "ITagHandler.d.ts",
                "IAnimationHandler.d.ts", "IActionManager.d.ts", "IAttributeHandler.d.ts",
                "ICustomEffectHandler.d.ts", "IMagicHandler.d.ts", "INaturalSpawnsHandler.d.ts",
                "IOverlayHandler.d.ts", "IPartyHandler.d.ts", "IPlayerBankData.d.ts",
                "IPlayerData.d.ts", "IPlayerDialogData.d.ts", "IPlayerFactionData.d.ts",
                "IPlayerItemGiverData.d.ts", "IPlayerMailData.d.ts", "IPlayerQuestData.d.ts",
                "IPlayerTransportData.d.ts", "IProfileHandler.d.ts", "ITransportHandler.d.ts"
            };
        } else if (dirPath.endsWith("item/")) {
            return new String[]{
                "IItemStack.d.ts", "IItemArmor.d.ts", "IItemBook.d.ts", "IItemBlock.d.ts",
                "IItemCustom.d.ts", "IItemCustomizable.d.ts", "IItemLinked.d.ts"
            };
        } else if (dirPath.endsWith("block/")) {
            return new String[]{
                "IBlockScripted.d.ts", "ITextPlane.d.ts"
            };
        } else if (dirPath.endsWith("gui/")) {
            return new String[]{
                "IButton.d.ts", "ICustomGui.d.ts", "ILabel.d.ts", "ITextField.d.ts",
                "ITexturedRect.d.ts", "IScroll.d.ts", "IItemSlot.d.ts", "ICustomGuiComponent.d.ts",
                "ILine.d.ts"
            };
        }
        return new String[]{};
    }
    
    /**
     * Initialize the registry from a directory containing .d.ts files.
     */
    public void initializeFromDirectory(File directory) {
        if (initialized) return;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);
            parser.parseDirectory(directory);
            resolveInheritance();
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks");
        } catch (IOException e) {
            System.err.println("[JSTypeRegistry] Failed to load type definitions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize the registry from a VSIX file.
     */
    public void initializeFromVsix(File vsixFile) {
        if (initialized) return;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);
            parser.parseVsixArchive(vsixFile);
            resolveInheritance();
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks from VSIX");
        } catch (IOException e) {
            System.err.println("[JSTypeRegistry] Failed to load VSIX: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register a type.
     */
    public void registerType(JSTypeInfo type) {
        types.put(type.getFullName(), type);
    }
    
    /**
     * Register a type alias.
     */
    public void registerTypeAlias(String alias, String fullType) {
        typeAliases.put(alias, fullType);
    }
    
    /**
     * Register a hook function signature.
     */
    public void registerHook(String functionName, String paramName, String paramType) {
        hooks.computeIfAbsent(functionName, k -> new ArrayList<>())
             .add(new HookSignature(paramName, paramType));
    }
    
    /**
     * Get a type by name (handles aliases and primitives).
     */
    public JSTypeInfo getType(String name) {
        return getType(name, new HashSet<>());
    }
    
    /**
     * Internal method with cycle detection for type aliases.
     */
    private JSTypeInfo getType(String name, Set<String> visited) {
        if (name == null || name.isEmpty()) return null;
        
        // Strip array brackets for lookup
        String baseName = name.replace("[]", "").trim();
        
        // Check if primitive
        if (PRIMITIVES.contains(baseName)) {
            return null; // Primitives don't have JSTypeInfo
        }
        
        // Detect circular alias references
        if (visited.contains(baseName)) {
            System.err.println("[JSTypeRegistry] Circular type alias detected: " + baseName);
            return null;
        }
        visited.add(baseName);
        
        // Direct lookup
        if (types.containsKey(baseName)) {
            return types.get(baseName);
        }
        
        // Check type aliases
        if (typeAliases.containsKey(baseName)) {
            String resolved = typeAliases.get(baseName);
            return getType(resolved, visited);
        }
        
        // Try simple name lookup (for types like "IEntity" without namespace)
        for (JSTypeInfo type : types.values()) {
            if (type.getSimpleName().equals(baseName)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a type name is a known primitive.
     */
    public boolean isPrimitive(String typeName) {
        return PRIMITIVES.contains(typeName);
    }
    
    /**
     * Get hook signatures for a function name.
     */
    public List<HookSignature> getHookSignatures(String functionName) {
        return hooks.getOrDefault(functionName, Collections.emptyList());
    }
    
    /**
     * Check if a function name is a known hook.
     */
    public boolean isHook(String functionName) {
        return hooks.containsKey(functionName);
    }
    
    /**
     * Get the parameter type for a hook function.
     * If multiple overloads exist, returns the first one.
     */
    public String getHookParameterType(String functionName) {
        List<HookSignature> sigs = hooks.get(functionName);
        if (sigs != null && !sigs.isEmpty()) {
            return sigs.get(0).paramType;
        }
        return null;
    }
    
    /**
     * Resolve inheritance relationships between types.
     */
    private void resolveInheritance() {
        for (JSTypeInfo type : types.values()) {
            String extendsType = type.getExtendsType();
            if (extendsType != null) {
                JSTypeInfo parent = getType(extendsType);
                if (parent != null) {
                    type.setResolvedParent(parent);
                }
            }
        }
    }
    
    /**
     * Get all registered types.
     */
    public Collection<JSTypeInfo> getAllTypes() {
        return types.values();
    }
    
    /**
     * Get all type names.
     */
    public Set<String> getTypeNames() {
        return types.keySet();
    }
    
    /**
     * Get all hook function names.
     */
    public Set<String> getHookNames() {
        return hooks.keySet();
    }
    
    /**
     * Get all registered hooks.
     */
    public Map<String, List<HookSignature>> getAllHooks() {
        return hooks;
    }
    
    /**
     * Check if initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Clear the registry (for reloading).
     */
    public void clear() {
        types.clear();
        typeAliases.clear();
        hooks.clear();
        initialized = false;
    }
    
    /**
     * Represents a hook function signature.
     */
    public static class HookSignature {
        public final String paramName;
        public final String paramType;
        public final String doc;
        
        public HookSignature(String paramName, String paramType) {
            this(paramName, paramType, null);
        }
        
        public HookSignature(String paramName, String paramType, String doc) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.doc = doc;
        }
        
        @Override
        public String toString() {
            return paramName + ": " + paramType;
        }
    }
}
