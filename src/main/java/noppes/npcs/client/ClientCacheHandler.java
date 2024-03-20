package noppes.npcs.client;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.data.*;
import noppes.npcs.util.CacheHashMap;

import java.util.HashMap;
import java.util.UUID;

public class ClientCacheHandler {
    private static final CacheHashMap<String, CacheHashMap.CachedObject<ImageData>> imageDataCache = new CacheHashMap<>((long) ConfigClient.CacheLife * 60 * 1000);
    public static final HashMap<Integer, Animation> animationCache = new HashMap<>();
    public static PlayerData playerData = new PlayerData();
    public static OverlayQuestTracking questTrackingOverlay = null;
    public static HashMap<Integer, OverlayCustom> customOverlays = new HashMap<>();
    public static HashMap<UUID, HashMap<Integer, SkinOverlay>> skinOverlays = new HashMap<>();
    public static HashMap<UUID, AnimationData> playerAnimations = new HashMap<>();

    public static Party party;

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
        ClientCacheHandler.animationCache.clear();
        ClientCacheHandler.questTrackingOverlay = null;
        ClientCacheHandler.customOverlays.clear();
        ClientCacheHandler.skinOverlays.clear();
        ClientCacheHandler.playerAnimations.clear();
        ClientCacheHandler.playerData.animationData.clearCache();
    }

    public static void clearSkinCache() {
        ClientCacheHandler.imageDataCache.clear();
        ClientCacheHandler.customOverlays.clear();
        ClientCacheHandler.skinOverlays.clear();
    }
}
