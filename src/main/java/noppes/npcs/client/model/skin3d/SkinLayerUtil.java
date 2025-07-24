package noppes.npcs.client.model.skin3d;

import java.awt.image.BufferedImage;

public class SkinLayerUtil {
    private static boolean hasSecondLayer(BufferedImage skin){
        // Support both square(64x64+) and wide(128x64) formats
        int w = skin.getWidth();
        int h = skin.getHeight();
        return h >= 64 || w >= 128;
    }

    private static int scale(float value,float scale){
        return Math.round(value*scale);
    }

    public static PixelModelPart buildHeadLayer(BufferedImage skin){
        if(skin==null || !hasSecondLayer(skin)) return null;
        float s = skin.getWidth()/64f;
        return TransparentPixelWrapper.wrapBox(skin,scale(8,s),scale(8,s),scale(8,s),scale(32,s),0);
    }

    public static PixelModelPart buildBodyLayer(BufferedImage skin){
        if(skin==null || !hasSecondLayer(skin)) return null;
        float s = skin.getWidth()/64f;
        return TransparentPixelWrapper.wrapBox(skin,scale(8,s),scale(12,s),scale(4,s),scale(16,s),scale(32,s));
    }

    public static PixelModelPart buildArmLayer(BufferedImage skin, boolean left, boolean slim){
        if(skin==null || !hasSecondLayer(skin)) return null;
        float s = skin.getWidth()/64f;
        int w = slim ? 3 : 4;
        int u = left ? 40 : 48;
        int v = left ? 32 : 48;
        return TransparentPixelWrapper.wrapBox(skin,scale(w,s),scale(12,s),scale(4,s),scale(u,s),scale(v,s));
    }

    public static PixelModelPart buildLegLayer(BufferedImage skin, boolean left){
        if(skin==null || !hasSecondLayer(skin)) return null;
        float s = skin.getWidth()/64f;
        int v = left ? 32 : 48;
        return TransparentPixelWrapper.wrapBox(skin,scale(4,s),scale(12,s),scale(4,s),0,scale(v,s));
    }
}
