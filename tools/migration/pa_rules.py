"""
PA Generation Rules — Single Source of Truth for Migration
==========================================================

This module defines ALL rules governing platform-abstraction (PA) generation.
Both analyze.py and scan_codebase.py import from here.

DESIGN PRINCIPLE: Every section is a flat data structure (dict, list, or scalar).
To add a new rule, just append an entry. No classes, no inheritance, no builders.

HOW TO USE:
    from pa_rules import EXISTING_MAPPINGS          # replaces hardcoded KNOWN_PA
    from pa_rules import classify_type               # get classification for any MC type
    from pa_rules import get_pa_name                 # get semantic PA interface name
    from pa_rules import get_service_target           # get service routing for SERVICE types
    from pa_rules import resolve_param_type           # resolve MC type → PA type for params

HOW TO EXTEND:
    - New PA mapping completed?   → Add to EXISTING_MAPPINGS
    - New type to classify?       → Add to FORCE_INTERFACE, FORCE_SERVICE, FORCE_TRANSFORM, or SUPPRESS
    - New semantic name?          → Add to NAMING
    - New service routing?        → Add to SERVICE_ROUTING
    - New obfuscated method name? → Add to OBFUSCATED_METHOD_NAMES
"""

import re

# ============================================================
# SECTION 1: EXISTING MAPPINGS
# Types already abstracted in platform-api.
# Used as the single source of truth for readiness scoring.
#
# Format: "net.minecraft.fully.qualified.Name": "PAInterfaceName"
#
# HOW TO UPDATE: When you complete a PA interface migration,
# add the MC FQN → PA name mapping here. Both analyze.py and
# scan_codebase.py will pick it up automatically.
# ============================================================

EXISTING_MAPPINGS = {
    # --- NBT ---
    "net.minecraft.nbt.NBTTagCompound": "INbt",
    "net.minecraft.nbt.NBTTagList": "INbtList",
    # --- Entity ---
    "net.minecraft.entity.Entity": "IEntity",
    "net.minecraft.entity.EntityLivingBase": "IEntityLivingBase",
    "net.minecraft.entity.EntityLiving": "IEntityLiving",
    "net.minecraft.entity.player.EntityPlayer": "IPlayer",
    "net.minecraft.entity.player.EntityPlayerMP": "IPlayer",
    # --- Item ---
    "net.minecraft.item.ItemStack": "IItemStack",
    # --- World ---
    "net.minecraft.world.World": "IWorld",
    "net.minecraft.world.WorldServer": "IWorld",
    # --- Combat ---
    "net.minecraft.util.DamageSource": "IDamageSource",
}


# ============================================================
# SECTION 2: TYPE CLASSIFICATION
# Controls how unabstracted MC types are handled during migration.
# Every MC type falls into exactly ONE category:
#
#   EXISTING   — Already has a PA interface (in EXISTING_MAPPINGS)
#   INTERFACE  — Should become a standalone PA interface (I*.java)
#   SERVICE    — Methods should be added to PlatformService or a domain service
#   TRANSFORM  — Handled by Static-Transform regex replacement
#   SUPPRESS   — Should NEVER be abstracted (client-only, debug, stays in mc1710)
#   AUTO       — Not explicitly classified; use heuristics (surface-miner data)
#
# HOW TO ADD: Put the MC simple class name (not FQN) in the right list.
# ============================================================

# --- Auto-classification thresholds (for types not in any list) ---
# Used by the classify command when a type isn't explicitly categorized.

# If more than this fraction of a type's used methods are static → SERVICE
STATIC_THRESHOLD = 0.6

# If a type has fewer than this many unique methods used → SERVICE (not worth an interface)
MIN_METHODS_FOR_INTERFACE = 3

# If a type is used in fewer than this many files → consider suppressing
MIN_FILES_FOR_INTERFACE = 5


# --- INTERFACE: Types that MUST become standalone PA interfaces ---
# These have rich instance-method surfaces and appear as parameters/return types.
# They represent concepts that exist across MC versions.
FORCE_INTERFACE = [
    # Command/Chat system
    "ICommandSender",  # 5 instance methods, used for command execution context
    "IChatComponent",  # Chat message abstraction (→ ITextComponent)
    "ChatComponentText",  # Concrete text component (→ ITextComponent)
    "ChatComponentTranslation",  # Translatable text (→ ITextComponent)
    "ChatStyle",  # Text formatting (→ ITextStyle)
    # Geometry / Math
    "AxisAlignedBB",  # Bounding boxes (→ IBoundingBox)
    "Vec3",  # 3D vector (→ IVector3)
    "MovingObjectPosition",  # Raytrace result (→ IRayTraceResult)
    # Resources
    "ResourceLocation",  # Asset/registry identifiers (→ IResourceLocation)
    # Effects
    "Potion",  # Potion type registry (→ IPotionType)
    "PotionEffect",  # Active potion effect instance (→ IPotionEffect)
    # Enchantment
    "Enchantment",  # Enchantment type (→ IEnchantment)
    # Auth
    "GameProfile",  # Mojang auth profile (→ IGameProfile)
    # NBT extension (if needed beyond INbt)
    "NBTBase",  # Base NBT tag (→ INbtBase, if needed)
]


# --- SERVICE: Types whose methods → PlatformService or domain service ---
# These are static utility classes or singleton accessors.
# Java 7 can't have static methods in interfaces, so these MUST go to services.
FORCE_SERVICE = [
    "CompressedStreamTools",  # → NBTIO: readCompressed, writeCompressed, read
    "PlayerSelector",  # → PlatformService: matchPlayers (1 static method)
    "MinecraftServer",  # → PlatformService: getServer, isDedicatedServer, worldServerForDimension
    "StringTranslate",  # → PlatformService: translate
    "FMLCommonHandler",  # → PlatformService: getSide, getEffectiveSide
    "ServerConfigurationManager",  # → PlatformService: getPlayerList, sendPacketToAllPlayers
    "JsonToNBT",  # → PlatformService: parseNbt (1 static method)
    "StatCollector",  # → PlatformService: translateToLocal, translateToLocalFormatted
]


# --- TRANSFORM: Types handled by regex-based Static-Transform ---
# These are annotations, enums, or trivially-replaceable patterns.
# The actual regex patterns are in TRANSFORM_PATTERNS below.
FORCE_TRANSFORM = [
    "Side",  # @SideOnly(Side.CLIENT) → @ClientOnly
    "SideOnly",  # Annotation carrier (handled by Side patterns)
    "MathHelper",  # MathHelper.clamp_int → ValueUtil.clampInt (pure static math)
    "EnumChatFormatting",  # Color codes → string constants or ITextFormatting enum
    "EnumFacing",  # Direction enum (if used in core)
]


# --- SUPPRESS: Types that should NEVER be abstracted ---
# Client-only, networking, debug, entity AI internals, registration.
# These stay in mc1710 forever. Don't waste time classifying them.
SUPPRESS = [
    # ---- Client-only (GUI, rendering) — never goes to core ----
    "GuiButton",
    "GuiScreen",
    "GuiTextField",
    "GuiContainer",
    "GuiSlot",
    "Minecraft",
    "FontRenderer",
    "Tessellator",
    "RenderHelper",
    "GL11",
    "GL12",
    "OpenGlHelper",
    "RenderGlobal",
    "TextureManager",
    "IIcon",
    "ScaledResolution",
    "RenderManager",
    "Render",
    "RenderLiving",
    "RenderPlayer",
    "EntityRenderer",
    "TileEntitySpecialRenderer",
    "ModelBase",
    "ModelRenderer",
    "ModelBiped",
    "ModelPlayer",
    "EnumParticleTypes",
    "WorldRenderer",
    "AbstractClientPlayer",
    "EntityClientPlayerMP",
    "ResourcePack",
    "IResourcePack",
    # ---- Networking — stays in mc1710 platform layer ----
    "Packet",
    "PacketBuffer",
    "ByteBuf",
    "ByteBufInputStream",
    "SimpleNetworkWrapper",
    "IMessage",
    "IMessageHandler",
    "MessageContext",
    "FMLProxyPacket",
    # ---- Entity AI internals — entities extend MC directly ----
    "EntityAIBase",
    "EntityAITasks",
    "PathNavigate",
    "DataWatcher",
    "EntityAITarget",
    "EntityAIWatchClosest",
    "PathFinder",
    "NavigationPath",
    "PathPoint",
    "PathEntity",
    # ---- Debug / Internal ----
    "Profiler",
    "CrashReport",
    "CrashReportCategory",
    # ---- Forge event system — stays in mc1710 ----
    "Event",
    "EventBus",
    "SubscribeEvent",
    "ForgeEventFactory",
    "MinecraftForge",
    "FMLEvent",
    "FMLPreInitializationEvent",
    "FMLInitializationEvent",
    "FMLPostInitializationEvent",
    "FMLServerStartingEvent",
    "FMLServerStoppingEvent",
    # ---- Registration — platform-specific forever ----
    "GameRegistry",
    "LanguageRegistry",
    "EntityRegistry",
    "NetworkRegistry",
    "TickRegistry",
    # ---- Mixin internals ----
    "SpongePowered",
    "Mixin",
    "Shadow",
    "Inject",
    # ---- Recipe system (NEVER list in AGENTS.md) ----
    "ShapedRecipes",
    "ShapelessRecipes",
    "CraftingManager",
    "IRecipe",
    # ---- Block/Tile internals that stay platform-side ----
    # (IBlock/ITileEntity exist but wrap ScriptedBlock, not raw MC Block)
    "CreativeTabs",
    "MapColor",
    # ---- Inventory internals ----
    "InventoryBasic",
    "InventoryPlayer",
]


# ============================================================
# SECTION 3: NAMING
# Semantic name overrides for generated PA interfaces.
# Maps MC simple class name → PA interface name.
#
# WHY: MC names are version-specific (ChatComponentText in 1.7,
# TextComponent in 1.16, Component in 1.19). PA names must be
# version-agnostic and stable across all versions.
#
# DEFAULT: If not listed here, the generated name is "I" + MCClassName.
#
# HOW TO ADD: When you decide on a semantic name for a type,
# add it here. The classify and generate-pa commands use this.
# ============================================================

NAMING = {
    # ---- Chat / Text system ----
    # 1.7: ChatComponentText, 1.16: StringTextComponent, 1.19: Component.literal()
    "ChatComponentText": "ITextComponent",
    "ChatComponentTranslation": "ITextComponent",
    "ChatComponentScore": "ITextComponent",
    "IChatComponent": "ITextComponent",
    "ChatStyle": "ITextStyle",
    "EnumChatFormatting": "ITextFormatting",
    # ---- Server ----
    # 1.7: MinecraftServer.getServer(), 1.13+: different access patterns
    "MinecraftServer": "IServer",
    "DedicatedServer": "IServer",
    "IntegratedServer": "IServer",
    "ServerConfigurationManager": "IPlayerManager",
    # ---- Geometry ----
    # 1.7: AxisAlignedBB, 1.14+: same name but different package
    "AxisAlignedBB": "IBoundingBox",
    "Vec3": "IVector3",
    "ChunkCoordinates": "IPos",  # IPos already exists in PA
    "MovingObjectPosition": "IRayTraceResult",
    # ---- Effects ----
    # 1.7: Potion, 1.9+: different registry system but same concept
    "Potion": "IPotionType",
    "PotionEffect": "IPotionEffect",
    # ---- Entity merges ----
    # Multiple MC classes → single PA interface (wraps closest common ancestor)
    "EntityPlayer": "IPlayer",
    "EntityPlayerMP": "IPlayer",
    "EntityLivingBase": "IEntityLivingBase",
    "EntityLiving": "IEntityLiving",
    "EntityCreature": "IEntityLiving",
    # ---- Resources ----
    # 1.7: ResourceLocation, all versions: same concept
    "ResourceLocation": "IResourceLocation",
    # ---- Auth ----
    "GameProfile": "IGameProfile",
    # ---- Enchantment ----
    "Enchantment": "IEnchantment",
    # ---- NBT base ----
    "NBTBase": "INbtBase",
    # ---- World ----
    "WorldServer": "IWorld",
    # ---- Command ----
    "ICommandSender": "ICommandSender",  # Keep same name (already good)
}


# ============================================================
# SECTION 4: ROUTING
# Where generated interfaces and service methods go.
#
# HOW TO UPDATE:
# - New sub-package needed? → Add a pattern to PACKAGE_ROUTING
# - New service target? → Add to SERVICE_ROUTING
# ============================================================

# Default output locations
DEFAULT_INTERFACE_PACKAGE = "noppes.npcs.api"
DEFAULT_INTERFACE_DIR = "platform-api/src/main/java/noppes/npcs/api/"

# Sub-package routing: regex on PA interface name → sub-package
# First match wins. If nothing matches → DEFAULT_INTERFACE_PACKAGE.
PACKAGE_ROUTING = [
    (r"IPlayer|IEntity|ILiving|ICustomNpc", "noppes.npcs.api.entity"),
    (r"IItem", "noppes.npcs.api.item"),
    (r"IScoreboard", "noppes.npcs.api.scoreboard"),
    (r"IOverlay", "noppes.npcs.api.overlay"),
    (r"IJob", "noppes.npcs.api.jobs"),
    (r"IRole", "noppes.npcs.api.roles"),
    (r"IHandler|IPlayerData|IPlayerQuest", "noppes.npcs.api.handler"),
    # Everything else → default package
]

# Service routing: MC simple name → service target
# Defines WHERE service methods go for FORCE_SERVICE types.
SERVICE_ROUTING = {
    "CompressedStreamTools": {
        "service": "NBTIO",
        "file": "platform-api/src/main/java/noppes/npcs/platform/nbt/NBTIO.java",
        "reason": "NBT I/O operations belong with NBT infrastructure",
    },
    "PlayerSelector": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Single static method (matchPlayers), too small for own service",
    },
    "MinecraftServer": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Server access is a core platform concern (getServer, isDedicatedServer)",
    },
    "StringTranslate": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Translation is platform-specific (different I18n in each version)",
    },
    "FMLCommonHandler": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Side detection is platform infrastructure",
    },
    "ServerConfigurationManager": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Player management access is core platform concern",
    },
    "JsonToNBT": {
        "service": "NBTIO",
        "file": "platform-api/src/main/java/noppes/npcs/platform/nbt/NBTIO.java",
        "reason": "JSON↔NBT conversion belongs with NBT infrastructure",
    },
    "StatCollector": {
        "service": "PlatformService",
        "file": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
        "reason": "Translation/localization is platform-specific",
    },
}


# ============================================================
# SECTION 5: STATIC TRANSFORM PATTERNS
# Regex patterns for FORCE_TRANSFORM types.
# Used by Static-Transform.ps1 during migration waves.
#
# Format: "MCTypeName": [(regex_pattern, replacement), ...]
#
# HOW TO ADD: When you identify a new pattern for a TRANSFORM type,
# add the regex + replacement here. The classify command shows
# these patterns in its output.
# ============================================================

TRANSFORM_PATTERNS = {
    "Side": [
        (r"@SideOnly\s*\(\s*Side\.CLIENT\s*\)", "@ClientOnly"),
        (r"@SideOnly\s*\(\s*Side\.SERVER\s*\)", "@ServerOnly"),
    ],
    "SideOnly": [],  # Handled by Side patterns above
    "MathHelper": [
        (r"MathHelper\.clamp_int\s*\(", "ValueUtil.clampInt("),
        (r"MathHelper\.clamp_float\s*\(", "ValueUtil.clampFloat("),
        (r"MathHelper\.clamp_double\s*\(", "ValueUtil.clampDouble("),
        (r"MathHelper\.sqrt_double\s*\(", "Math.sqrt("),
        (r"MathHelper\.sqrt_float\s*\(", "(float)Math.sqrt("),
        (r"MathHelper\.floor_double\s*\(", "ValueUtil.floorDouble("),
        (r"MathHelper\.floor_float\s*\(", "ValueUtil.floorFloat("),
    ],
    "EnumChatFormatting": [
        # Typically replaced with string constants or suppressed in core
        # Add specific patterns as migration progresses
    ],
    "EnumFacing": [
        # Direction enum — add patterns when core code needs directions
    ],
}


# ============================================================
# SECTION 6: PARAMETER TYPE RESOLUTION
# When generating PA interface method signatures, MC types in
# parameters and return types need to be resolved to PA types.
#
# Strategy for types NOT in this map:
#   "object" — use Object (pragmatic, safe, fast)
#   "recursive" — also generate interfaces for param types (thorough)
# ============================================================

UNRESOLVED_PARAM_STRATEGY = "object"

# Known parameter type mappings: MC simple name → PA type name
# Used when generating interface method signatures.
PARAM_TYPE_MAP = {
    # ---- Already abstracted (mirrors EXISTING_MAPPINGS by simple name) ----
    "EntityPlayer": "IPlayer",
    "EntityPlayerMP": "IPlayer",
    "NBTTagCompound": "INbt",
    "NBTTagList": "INbtList",
    "ItemStack": "IItemStack",
    "World": "IWorld",
    "WorldServer": "IWorld",
    "DamageSource": "IDamageSource",
    "Entity": "IEntity",
    "EntityLivingBase": "IEntityLivingBase",
    "EntityLiving": "IEntityLiving",
    # ---- Planned abstractions (use the NAMING value) ----
    "ResourceLocation": "IResourceLocation",
    "ChatComponentText": "ITextComponent",
    "ChatComponentTranslation": "ITextComponent",
    "IChatComponent": "ITextComponent",
    "AxisAlignedBB": "IBoundingBox",
    "Vec3": "IVector3",
    "GameProfile": "IGameProfile",
    "PotionEffect": "IPotionEffect",
    "Potion": "IPotionType",
    "Enchantment": "IEnchantment",
    "ICommandSender": "ICommandSender",
    "MovingObjectPosition": "IRayTraceResult",
    # ---- Java primitives (pass through unchanged) ----
    "String": "String",
    "int": "int",
    "float": "float",
    "double": "double",
    "boolean": "boolean",
    "long": "long",
    "byte": "byte",
    "short": "short",
    "char": "char",
    "void": "void",
    "byte[]": "byte[]",
    "int[]": "int[]",
    "String[]": "String[]",
    "float[]": "float[]",
    "double[]": "double[]",
    # ---- Java standard library (pass through unchanged) ----
    "List": "List",
    "ArrayList": "List",
    "Map": "Map",
    "HashMap": "Map",
    "Set": "Set",
    "HashSet": "Set",
    "Collection": "Collection",
    "File": "File",
    "UUID": "UUID",
    "InputStream": "InputStream",
    "OutputStream": "OutputStream",
    "DataInputStream": "DataInputStream",
    "DataOutputStream": "DataOutputStream",
    "Object": "Object",
    "Runnable": "Runnable",
}


# ============================================================
# SECTION 7: METHOD FILTERING
# Controls which methods are included/excluded in generated interfaces.
# ============================================================

# Methods to ALWAYS exclude from generated interfaces
# (inherited Object methods that never belong in PA interfaces)
EXCLUDED_METHODS = [
    "toString",
    "hashCode",
    "equals",
    "clone",
    "finalize",
    "getClass",
    "notify",
    "notifyAll",
    "wait",
]

# Obfuscated MCP method names → readable names
# When surface-miner finds these, use the readable name in generated interfaces.
OBFUSCATED_METHOD_NAMES = {
    "func_152457_a": "decompress",
    "func_150296_c": "getTagType",
    "func_150299_b": "getKeySet",
    "func_74875_a": "getTagList",
    "func_82580_o": "getShort",
    "func_150295_c": "merge",
    "func_74737_a": "copy",
    "func_74732_a": "getId",
}

# If True, obfuscated methods (matching func_\d+_[a-z]) that are NOT in
# OBFUSCATED_METHOD_NAMES are excluded from generated interfaces.
EXCLUDE_UNMAPPED_OBFUSCATED = True


# ============================================================
# SECTION 8: JAVA CONSTRAINTS
# Compile-time constraints that affect generated code shape.
# ============================================================

# Java language level for generated interfaces.
# Java 7: no default methods, no static interface methods.
# Java 8: default and static methods allowed.
JAVA_TARGET = 7

# If True, add @Override on wrapper methods implementing PA interfaces.
ADD_OVERRIDE = True


# ============================================================
# SECTION 9: HELPER FUNCTIONS
# Convenience functions for consuming the rules above.
# These are the primary API for other modules.
# ============================================================


def _simple_name(fqn_or_simple):
    """Extract simple class name from FQN or return as-is."""
    return fqn_or_simple.rsplit(".", 1)[-1] if "." in fqn_or_simple else fqn_or_simple


def is_existing(fqn):
    """Check if a fully-qualified MC type already has a PA abstraction."""
    return fqn in EXISTING_MAPPINGS


def get_existing_pa(fqn):
    """Get the PA interface name for an already-abstracted MC type, or None."""
    return EXISTING_MAPPINGS.get(fqn)


def classify_type(simple_name, surface_data=None):
    """
    Classify an MC type into a migration category.

    Args:
        simple_name: MC class simple name (e.g. "MinecraftServer")
        surface_data: Optional dict from surface-miner with keys:
            "methods": [{"name": str, "kind": "static"|"instance", "count": int}, ...]
            If provided, enables AUTO classification with heuristics.

    Returns:
        dict with keys:
            "classification": "EXISTING"|"INTERFACE"|"SERVICE"|"TRANSFORM"|"SUPPRESS"|"AUTO"
            "reason": str explaining why
            "pa_name": str or None (the PA interface/service name)
            "target": str or None (service file path for SERVICE types)
            "transform_patterns": list or None (regex patterns for TRANSFORM types)
    """
    name = _simple_name(simple_name)

    # Check EXISTING first (by simple name match against EXISTING_MAPPINGS values)
    for fqn, pa in EXISTING_MAPPINGS.items():
        if _simple_name(fqn) == name:
            return {
                "classification": "EXISTING",
                "reason": f"Already abstracted as {pa}",
                "pa_name": pa,
                "target": None,
                "transform_patterns": None,
            }

    # Check SUPPRESS
    if name in SUPPRESS:
        return {
            "classification": "SUPPRESS",
            "reason": "Suppressed (client-only, debug, networking, or stays in mc1710)",
            "pa_name": None,
            "target": None,
            "transform_patterns": None,
        }

    # Check FORCE_TRANSFORM
    if name in FORCE_TRANSFORM:
        patterns = TRANSFORM_PATTERNS.get(name, [])
        return {
            "classification": "TRANSFORM",
            "reason": f"Static-Transform ({len(patterns)} regex patterns)",
            "pa_name": None,
            "target": None,
            "transform_patterns": patterns,
        }

    # Check FORCE_SERVICE
    if name in FORCE_SERVICE:
        routing = SERVICE_ROUTING.get(name, {})
        return {
            "classification": "SERVICE",
            "reason": routing.get("reason", "Force-classified as service"),
            "pa_name": None,
            "target": routing.get("file"),
            "service_name": routing.get("service", "PlatformService"),
            "transform_patterns": None,
        }

    # Check FORCE_INTERFACE
    if name in FORCE_INTERFACE:
        pa_name = get_pa_name(name)
        return {
            "classification": "INTERFACE",
            "reason": "Force-classified as interface",
            "pa_name": pa_name,
            "target": _resolve_interface_path(pa_name),
            "transform_patterns": None,
        }

    # AUTO classification using surface data (if provided)
    if surface_data and surface_data.get("methods"):
        methods = surface_data["methods"]
        total = len(methods)
        static_count = sum(1 for m in methods if m.get("kind") == "static")
        static_ratio = static_count / total if total > 0 else 0

        if total < MIN_METHODS_FOR_INTERFACE:
            return {
                "classification": "SERVICE",
                "reason": f"Auto: only {total} methods (< {MIN_METHODS_FOR_INTERFACE} threshold)",
                "pa_name": None,
                "target": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
                "service_name": "PlatformService",
                "transform_patterns": None,
            }

        if static_ratio > STATIC_THRESHOLD:
            return {
                "classification": "SERVICE",
                "reason": f"Auto: {static_ratio:.0%} static methods (> {STATIC_THRESHOLD:.0%} threshold)",
                "pa_name": None,
                "target": "platform-api/src/main/java/kamkeel/npcs/platform/PlatformService.java",
                "service_name": "PlatformService",
                "transform_patterns": None,
            }

        pa_name = get_pa_name(name)
        return {
            "classification": "INTERFACE",
            "reason": f"Auto: {total} methods, {static_ratio:.0%} static → instance-heavy",
            "pa_name": pa_name,
            "target": _resolve_interface_path(pa_name),
            "transform_patterns": None,
        }

    # No data, no explicit rule → UNKNOWN
    return {
        "classification": "AUTO",
        "reason": "Not classified. Run with --surface to enable auto-classification.",
        "pa_name": get_pa_name(name),
        "target": None,
        "transform_patterns": None,
    }


def get_pa_name(simple_name):
    """
    Get the PA interface name for an MC type.
    Uses NAMING overrides, falls back to "I" + simple_name.
    """
    name = _simple_name(simple_name)
    return NAMING.get(name, f"I{name}")


def get_service_target(simple_name):
    """
    Get the service routing for a SERVICE-classified type.
    Returns dict with 'service', 'file', 'reason' or None.
    """
    name = _simple_name(simple_name)
    return SERVICE_ROUTING.get(name)


def resolve_param_type(mc_type_name):
    """
    Resolve an MC type name to its PA equivalent for use in method signatures.
    Returns the PA type name, or "Object" if unresolved (per UNRESOLVED_PARAM_STRATEGY).
    """
    name = _simple_name(mc_type_name)
    resolved = PARAM_TYPE_MAP.get(name)
    if resolved:
        return resolved
    if UNRESOLVED_PARAM_STRATEGY == "object":
        return "Object"
    return name  # "recursive" strategy — caller handles generation


def should_exclude_method(method_name):
    """Check if a method should be excluded from generated interfaces."""
    if method_name in EXCLUDED_METHODS:
        return True
    if EXCLUDE_UNMAPPED_OBFUSCATED and re.match(r"func_\d+_[a-z]", method_name):
        return method_name not in OBFUSCATED_METHOD_NAMES
    return False


def resolve_obfuscated_method(method_name):
    """Resolve an obfuscated MCP method name to readable, or return as-is."""
    return OBFUSCATED_METHOD_NAMES.get(method_name, method_name)


def get_interface_package(pa_name):
    """Determine which package a PA interface should go in based on its name."""
    for pattern, package in PACKAGE_ROUTING:
        if re.search(pattern, pa_name):
            return package
    return DEFAULT_INTERFACE_PACKAGE


def _resolve_interface_path(pa_name):
    """Build the full file path for a PA interface."""
    package = get_interface_package(pa_name)
    package_path = package.replace(".", "/")
    return f"platform-api/src/main/java/{package_path}/{pa_name}.java"


# ============================================================
# SECTION 10: QUICK REFERENCE
# Summary of all classified types for fast lookup.
# Run: python -m pa_rules to see the full classification table.
# ============================================================

if __name__ == "__main__":
    print("=" * 72)
    print("PA GENERATION RULES — CLASSIFICATION SUMMARY")
    print("=" * 72)

    print(f"\n  EXISTING ({len(EXISTING_MAPPINGS)} types):")
    for fqn, pa in sorted(EXISTING_MAPPINGS.items(), key=lambda x: x[1]):
        print(f"    {_simple_name(fqn):30s} → {pa}")

    print(f"\n  INTERFACE ({len(FORCE_INTERFACE)} types):")
    for t in sorted(FORCE_INTERFACE):
        pa = get_pa_name(t)
        path = _resolve_interface_path(pa)
        print(f"    {t:30s} → {pa:20s}  {path}")

    print(f"\n  SERVICE ({len(FORCE_SERVICE)} types):")
    for t in sorted(FORCE_SERVICE):
        routing = SERVICE_ROUTING.get(t, {})
        svc = routing.get("service", "PlatformService")
        print(f"    {t:30s} → {svc}")

    print(f"\n  TRANSFORM ({len(FORCE_TRANSFORM)} types):")
    for t in sorted(FORCE_TRANSFORM):
        patterns = TRANSFORM_PATTERNS.get(t, [])
        print(f"    {t:30s}   ({len(patterns)} patterns)")

    print(f"\n  SUPPRESS ({len(SUPPRESS)} types):")
    for i in range(0, len(SUPPRESS), 6):
        chunk = SUPPRESS[i : i + 6]
        print(f"    {', '.join(chunk)}")

    total = (
        len(EXISTING_MAPPINGS)
        + len(FORCE_INTERFACE)
        + len(FORCE_SERVICE)
        + len(FORCE_TRANSFORM)
        + len(SUPPRESS)
    )
    print(f"\n  TOTAL CLASSIFIED: {total}")
    print(f"  REMAINING AUTO:  {400 - total} (of ~400 MC types in codebase)")
    print(f"\n  Run 'python analyze.py classify <type>' for per-type classification.")
