package kamkeel.npcs.addon;

import net.minecraft.entity.EntityLivingBase;

public class GeckoAddon {
    public static GeckoAddon instance;
    public boolean supportEnabled = true;

    /**
     * This class is a shell class to be changed via mixins
     * The Addon Mod will replace all blank functions within
     * this class to change the ongoing code.
     */
    public GeckoAddon(){
        instance = this;
    }

    public void geckoCopyData(EntityLivingBase copied, EntityLivingBase entity){}

}
