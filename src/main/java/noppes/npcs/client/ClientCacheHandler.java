package noppes.npcs.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.hud.ClientHudManager;
import noppes.npcs.client.gui.hud.EnumHudComponent;
import noppes.npcs.client.gui.hud.HudComponent;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.SkinOverlay;
import kamkeel.npcs.network.enums.EnumSyncType;
import noppes.npcs.util.CacheHashMap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientCacheHandler {
    private static final CacheHashMap<String, CacheHashMap.CachedObject<ImageData>> imageDataCache = new CacheHashMap<>((long) ConfigClient.CacheLife * 60 * 1000);
    public static PlayerData playerData = new PlayerData();
    public static HashMap<Integer, OverlayCustom> customOverlays = new HashMap<>();
    public static HashMap<UUID, HashMap<Integer, SkinOverlay>> skinOverlays = new HashMap<>();
    public static HashMap<UUID, AnimationData> playerAnimations = new HashMap<>();

    private static String activeServerKey = "";
    private static final Map<String, EnumMap<EnumSyncType, Integer>> clientRevisionCache = new HashMap<>();

    public static Party party;

    public static boolean allowProfiles = true;
    public static boolean allowParties = true;

    public static void setActiveServer(String serverKey, EnumMap<EnumSyncType, Integer> serverRevisions) {
        String normalizedKey = serverKey == null ? "" : serverKey;
        if (!normalizedKey.equals(activeServerKey)) {
            clientRevisionCache.clear();
        }
        activeServerKey = normalizedKey;

        if (activeServerKey.isEmpty()) {
            return;
        }

        EnumMap<EnumSyncType, Integer> cached = clientRevisionCache.computeIfAbsent(
            activeServerKey,
            ignored -> new EnumMap<>(EnumSyncType.class)
        );
        if (serverRevisions != null && !serverRevisions.isEmpty()) {
            cached.keySet().retainAll(serverRevisions.keySet());
        }
    }

    public static EnumMap<EnumSyncType, Integer> getCachedRevisionsForServer(String serverKey) {
        String key = serverKey == null ? "" : serverKey;
        if (key.isEmpty()) {
            return new EnumMap<>(EnumSyncType.class);
        }
        EnumMap<EnumSyncType, Integer> revisions = clientRevisionCache.get(key);
        if (revisions == null) {
            return new EnumMap<>(EnumSyncType.class);
        }
        return new EnumMap<>(revisions);
    }

    public static void updateClientRevision(EnumSyncType type, int revision) {
        if (revision < 0) {
            return;
        }
        String key = activeServerKey == null ? "" : activeServerKey;
        if (key.isEmpty()) {
            return;
        }
        EnumMap<EnumSyncType, Integer> revisions = clientRevisionCache.computeIfAbsent(
            key,
            ignored -> new EnumMap<>(EnumSyncType.class)
        );
        revisions.put(type, revision);
    }


    public static ImageData getImageData(String directory) {
        synchronized (imageDataCache) {
            if (!imageDataCache.containsKey(directory)) {
                imageDataCache.put(directory, new CacheHashMap.CachedObject<>(new ImageData(directory)));
            }
            return imageDataCache.get(directory).getObject();
        }
    }

    public static ImageData getNPCTexture(String directory, boolean x64, ResourceLocation resource) {
        synchronized (imageDataCache) {
            if (!imageDataCache.containsKey(resource.getResourcePath())) {
                imageDataCache.put(resource.getResourcePath(), new CacheHashMap.CachedObject<>(new ImageData(directory, x64, resource)));
            }
            return imageDataCache.get(resource.getResourcePath()).getObject();
        }
    }

    public static boolean isCachedNPC(ResourceLocation resource) {
        synchronized (imageDataCache) {
            return imageDataCache.containsKey(resource.getResourcePath());
        }
    }

    public static void clearCache() {
        ClientCacheHandler.imageDataCache.clear();
        ClientCacheHandler.customOverlays.clear();
        ClientCacheHandler.skinOverlays.clear();
        ClientCacheHandler.playerAnimations.clear();

        // Clear Texture Caches
        GuiSoundSelection.cachedDomains.clear();
        GuiTextureSelection.cachedTextures.clear();

        // Clear music/bard sounds
        MusicController.Instance.stopAllSounds();

        // Clear Quest Tracker
        HudComponent component = ClientHudManager.getInstance().getHudComponents().get(EnumHudComponent.QuestTracker);
        if (component != null) {
            component.loadData(new NBTTagCompound());
            component.hasData = false;
        }
    }

    public static void clearSkinCache() {
        ClientCacheHandler.imageDataCache.clear();
        ClientCacheHandler.customOverlays.clear();
        ClientCacheHandler.skinOverlays.clear();

        // Clear Texture Caches
        GuiSoundSelection.cachedDomains.clear();
        GuiTextureSelection.cachedTextures.clear();
    }
}
