package noppes.npcs.config;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigMixin {
    public static IConfiguration config;

    public final static String CLIENT = "CLIENT";
    public final static String SERVER = "SERVER";
    public final static String GENERAL = "GENERAL";

    /**
     * General Properties
     **/
    public static IConfigProperty EntityRendererMixinProperty;
    public static boolean EntityRendererMixin = true;

    public static IConfigProperty AnimationMixinProperty;
    public static boolean AnimationMixin = true;

    public static IConfigProperty FirstPersonAnimationMixinProperty;
    public static boolean FirstPersonAnimationMixin = true;

    public static IConfigProperty EntitySpawnFixMixinProperty;
    public static boolean EntitySpawnFixMixin = true;

    public static void init(File configFile) {
        config = new IConfiguration(configFile);

        try {
            config.load();

            EntityRendererMixinProperty = config.get(CLIENT, "IEntity Render Mixin", true, "Enables Overlay Mixins for Conflicts relating to Optifine or other Skin Renderers. If crashes occur, please disable.");
            EntityRendererMixin = EntityRendererMixinProperty.getBoolean(true);

            AnimationMixinProperty = config.get(CLIENT, "Animation Mixin", true, "Enables mixins for the ModelRenderer and RenderPlayer classes, allowing for additional animation functionality in the API. If crashes or visual errors occur, please disable.");
            AnimationMixin = AnimationMixinProperty.getBoolean(true);

            if (AnimationMixin) {
                FirstPersonAnimationMixinProperty = config.get(CLIENT, "First Person Animation Mixin", true, "Enables mixins for the ItemRenderer class, allowing for animations to be visible in first person. Can only be enabled if the animation mixin is enabled. If crashes or visual errors occur, please disable.");
                FirstPersonAnimationMixin = FirstPersonAnimationMixinProperty.getBoolean(true);
            } else {
                FirstPersonAnimationMixin = false;
            }

            EntitySpawnFixMixinProperty = config.get(SERVER, "IEntity Spawn Packet Fix", true, "Fixes a Forge race condition where mod IEntity spawn packets can be lost or sent to the wrong player. This prevents invisible entities on servers with multiple players.");
            EntitySpawnFixMixin = EntitySpawnFixMixinProperty.getBoolean(true);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its mixin IConfiguration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
