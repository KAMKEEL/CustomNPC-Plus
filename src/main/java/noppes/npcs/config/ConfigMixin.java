package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigMixin
{
    public static Configuration config;

    public final static String CLIENT = "CLIENT";
    public final static String GENERAL = "GENERAL";

    /**
     *  General Properties
     **/
    public static Property EntityRendererMixinProperty;
    public static boolean EntityRendererMixin = true;

    public static Property AnimationMixinProperty;
    public static boolean AnimationMixin = true;

    public static Property FirstPersonAnimationMixinProperty;
    public static boolean FirstPersonAnimationMixin = true;

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();

            EntityRendererMixinProperty = config.get(CLIENT, "Entity Render Mixin", true, "Enables Overlay Mixins for Conflicts relating to Optifine or other Skin Renderers. If crashes occur, please disable.");
            EntityRendererMixin = EntityRendererMixinProperty.getBoolean(true);

            AnimationMixinProperty = config.get(CLIENT, "Animation Mixin", true, "Enables mixins for the ModelRenderer and RenderPlayer classes, allowing for additional animation functionality in the API. If crashes or visual errors occur, please disable.");
            AnimationMixin = AnimationMixinProperty.getBoolean(true);

            if (AnimationMixin) {
                FirstPersonAnimationMixinProperty = config.get(CLIENT, "First Person Animation Mixin", true, "Enables mixins for the ItemRenderer class, allowing for animations to be visible in first person. Can only be enabled if the animation mixin is enabled. If crashes or visual errors occur, please disable.");
                FirstPersonAnimationMixin = FirstPersonAnimationMixinProperty.getBoolean(true);
            } else {
                FirstPersonAnimationMixin = false;
            }
        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its mixin configuration");
        }
        finally
        {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
