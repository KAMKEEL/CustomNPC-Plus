package noppes.npcs.client.model.skin3d;

import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.util.CacheHashMap;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

// classes for 3D layer building
import noppes.npcs.client.model.skin3d.PixelModelPart;
import noppes.npcs.client.model.skin3d.SkinLayerUtil;

public class SkinLayerCache {
    private static final CacheHashMap<String, CacheHashMap.CachedObject<LayerSet>> CACHE =
            new CacheHashMap<>((long) ConfigClient.CacheLife * 60 * 1000);

    public static class LayerSet {
        public PixelModelPart head;
        public PixelModelPart body;
        public PixelModelPart rightArm;
        public PixelModelPart leftArm;
        public PixelModelPart rightLeg;
        public PixelModelPart leftLeg;
    }

    private static String key(ResourceLocation loc, boolean slim){
        return loc.getResourcePath() + (slim ? "#slim" : "#normal");
    }

    public static LayerSet getLayers(ImageData data, boolean slim){
        if(data == null || data.getLocation() == null)
            return null;
        String k = key(data.getLocation(), slim);
        synchronized (CACHE){
            CacheHashMap.CachedObject<LayerSet> ref = CACHE.get(k);
            if(ref != null)
                return ref.getObject();
            LayerSet set = new LayerSet();
            BufferedImage img = data.getBufferedImage();
            if(img != null){
                set.head = SkinLayerUtil.buildHeadLayer(img);
                set.body = SkinLayerUtil.buildBodyLayer(img);
                set.rightArm = SkinLayerUtil.buildArmLayer(img, false, slim);
                set.leftArm = SkinLayerUtil.buildArmLayer(img, true, slim);
                set.rightLeg = SkinLayerUtil.buildLegLayer(img, false);
                set.leftLeg = SkinLayerUtil.buildLegLayer(img, true);
            }
            CACHE.put(k, new CacheHashMap.CachedObject<>(set));
            return set;
        }
    }

    public static void clearCache(){
        synchronized (CACHE){
            CACHE.clear();
        }
    }
}
