package noppes.npcs.client;

import noppes.npcs.client.renderer.customitem.ImageData;
import noppes.npcs.util.CacheHashMap;

public class ClientCache {
    private static final CacheHashMap<String, CacheHashMap.CachedObject<ImageData>> imageDataCache = new CacheHashMap<>(10*60*1000);

    public static ImageData getImageData(String directory) {
        synchronized (imageDataCache) {
            if (!imageDataCache.containsKey(directory)) {
                imageDataCache.put(directory, new CacheHashMap.CachedObject<>(new ImageData(directory)));
            }
            return imageDataCache.get(directory).getObject();
        }
    }
}
