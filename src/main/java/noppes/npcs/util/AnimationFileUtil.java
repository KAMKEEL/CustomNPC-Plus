package noppes.npcs.util;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.resource.GeckoLibCache;

import java.util.List;
import java.util.Vector;

public class AnimationFileUtil {
    public static List<String> getAnimationList(String animFileName) {
        Vector<String> list = new Vector<>();
        AnimationFile file = GeckoLibCache.getInstance().getAnimations().get(new ResourceLocation(animFileName));
        if (file != null) {
            for (Animation anim : file.getAllAnimations()) {
                list.add(anim.animationName);
            }
        }
        return list;
    }

    public static List<String> getAnimationFileList() {
        Vector<String> list = new Vector<>();
        for (ResourceLocation resLoc : GeckoLibCache.getInstance().getAnimations().keySet()) {
            list.add(resLoc.toString());
        }
        return list;
    }
}
