package noppes.npcs.client;

import noppes.npcs.client.renderer.customitem.ImageData;
import noppes.npcs.util.CacheHashMap;

public class ClientCache {
    private static final CacheHashMap<String, CacheHashMap.CachedObject<ImageData>> imageDataCache = new CacheHashMap<>(10*1000);

    public static ImageData getImageData(String directory) {
        if (!imageDataCache.containsKey(directory)) {
            ImageData imageData = new ImageData(directory);
            imageDataCache.put(directory, new CacheHashMap.CachedObject<>(imageData));
        }
        return imageDataCache.get(directory).getObject();
    }
}
