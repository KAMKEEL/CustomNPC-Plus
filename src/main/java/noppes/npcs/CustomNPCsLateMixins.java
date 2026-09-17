package noppes.npcs;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import noppes.npcs.config.ConfigMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The mixins into other mods' player models, loaded by UniMixins after every
 * mod jar is on the class path. Only found when UniMixins is installed; the
 * early plugin then leaves these out.
 */
@LateMixin
public class CustomNPCsLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.customnpcs.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (!FMLLaunchHandler.side().isClient() || !ConfigMixin.AnimationMixin) {
            return mixins;
        }
        mixins.addAll(OptionalModelMixins.playerModelMixins(true));
        System.out.println("[CustomNPC+] Late player model mixins: " + mixins);
        return mixins;
    }
}
