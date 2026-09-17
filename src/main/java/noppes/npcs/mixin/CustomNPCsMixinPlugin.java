package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import noppes.npcs.OptionalModelMixins;
import noppes.npcs.config.ConfigMixin;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomNPCsMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        System.out.println("[CustomNPC+] Mixin plugin loaded from " + mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // The targets are read by now; a jar lent so Mixin could read one goes back.
        OptionalModelMixins.releaseLentJars();
    }

    @Override
    public List<String> getMixins() {

        // Load Mixin Config
        String configPath = "config" + File.separator + "CustomNpcPlus" + File.separator;
        ConfigMixin.init(new File(configPath + "mixin.cfg"));
        boolean client = FMLLaunchHandler.side().isClient();
        System.out.println("[CustomNPC+] Choosing mixins: side=" + FMLLaunchHandler.side()
            + ", animation=" + ConfigMixin.AnimationMixin
            + ", firstPersonAnimation=" + ConfigMixin.FirstPersonAnimationMixin);

        List<String> mixins = new ArrayList<>();

        // Client Only Mixins
        if (client) {
            if (ConfigMixin.EntityRendererMixin) {
                mixins.add("MixinEntityRenderer");
            }
            if (ConfigMixin.AnimationMixin) {
                mixins.add("MixinModelRenderer");
                if (OptionalModelMixins.lateLoaderAvailable()) {
                    System.out.println("[CustomNPC+] UniMixins is here; the other mods' model mixins load late");
                } else {
                    for (String name : OptionalModelMixins.playerModelMixins(false)) {
                        mixins.add("late." + name);
                    }
                }
                mixins.add("MixinRendererLivingEntity");
            }
            System.out.println("[CustomNPC+] Client mixins chosen: " + mixins);
            if (ConfigMixin.FirstPersonAnimationMixin) {
                mixins.add("MixinItemRenderer");
            }

            mixins.add("MixinItemStack");
        }

        // The integrated server launches on the client side, so gating this on the launch
        // side would skip the tracker fix in singleplayer and LAN.
        if (ConfigMixin.EntitySpawnFixMixin) {
            mixins.add("MixinEntityTrackerEntry");
        }

        System.out.println("[CustomNPC+] Selected mixins: " + mixins);

        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (isPlayerCompatibilityMixin(mixinClassName)) {
            System.out.println("[CustomNPC+] Applying player animation mixin to " + targetClassName);
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (isPlayerCompatibilityMixin(mixinClassName)) {
            System.out.println("[CustomNPC+] Applied player animation mixin to " + targetClassName);
        }
    }

    private static boolean isPlayerCompatibilityMixin(String mixinClassName) {
        return mixinClassName.contains("MixinMPMModel")
            || mixinClassName.contains("MixinGalacticraftPlayerModel")
            || mixinClassName.contains("MixinPlayerAPIModel");
    }
}
