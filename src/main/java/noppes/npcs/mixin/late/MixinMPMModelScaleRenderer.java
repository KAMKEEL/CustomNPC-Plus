package noppes.npcs.mixin.late;

import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.AnimationMixinFunctions;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.controllers.data.AnimationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets CustomNPC+ animations reach MorePlayerModels+' replacement limbs.
 *
 * MPM's ModelScaleRenderer overrides ModelRenderer.render without calling the
 * parent implementation, so the normal animation mixin is bypassed. The
 * string target and {@link Pseudo} keep this integration entirely optional.
 */
@Pseudo
@Mixin(targets = "noppes.mpm.client.model.ModelScaleRenderer", remap = false)
public abstract class MixinMPMModelScaleRenderer {

    @Unique private boolean cnpc$changed;
    @Unique private float cnpc$pointX;
    @Unique private float cnpc$pointY;
    @Unique private float cnpc$pointZ;
    @Unique private float cnpc$angleX;
    @Unique private float cnpc$angleY;
    @Unique private float cnpc$angleZ;
    @Unique private float cnpc$offsetX;
    @Unique private float cnpc$offsetY;
    @Unique private float cnpc$offsetZ;
    @Unique private static boolean cnpc$loggedRenderHook;
    @Unique private static boolean cnpc$loggedAnimation;
    @Unique private static boolean cnpc$loggedChangedValues;

    @Inject(method = {"render(F)V", "func_78785_a(F)V"}, at = @At("HEAD"), remap = false, require = 1)
    private void cnpc$applyAnimation(float scale, CallbackInfo callbackInfo) {
        ModelRenderer renderer = (ModelRenderer) (Object) this;
        if (!cnpc$loggedRenderHook) {
            cnpc$loggedRenderHook = true;
            System.out.println("[CustomNPC+] MPM ModelScaleRenderer render hook is running; model="
                + renderer.baseModel.getClass().getName());
        }
        cnpc$pointX = renderer.rotationPointX;
        cnpc$pointY = renderer.rotationPointY;
        cnpc$pointZ = renderer.rotationPointZ;
        cnpc$angleX = renderer.rotateAngleX;
        cnpc$angleY = renderer.rotateAngleY;
        cnpc$angleZ = renderer.rotateAngleZ;
        cnpc$offsetX = renderer.offsetX;
        cnpc$offsetY = renderer.offsetY;
        cnpc$offsetZ = renderer.offsetZ;

        try {
            cnpc$changed = AnimationMixinFunctions.applyValues(renderer);
            if (ClientEventHandler.renderingPlayer != null) {
                AnimationData data = ClientCacheHandler.playerAnimations.get(
                    ClientEventHandler.renderingPlayer.getUniqueID());
                if (data != null && data.animation != null && data.isActive()) {
                    boolean valuesChanged = cnpc$pointX != renderer.rotationPointX
                        || cnpc$pointY != renderer.rotationPointY
                        || cnpc$pointZ != renderer.rotationPointZ
                        || cnpc$angleX != renderer.rotateAngleX
                        || cnpc$angleY != renderer.rotateAngleY
                        || cnpc$angleZ != renderer.rotateAngleZ;
                    if (!cnpc$loggedAnimation) {
                        cnpc$loggedAnimation = true;
                        System.out.println("[CustomNPC+] MPM render hook sees active animation for "
                            + ClientEventHandler.renderingPlayer.getCommandSenderName());
                    }
                    if (valuesChanged && !cnpc$loggedChangedValues) {
                        cnpc$loggedChangedValues = true;
                        System.out.println("[CustomNPC+] MPM animation changed limb values; limb="
                            + renderer.getClass().getName());
                    }
                }
            }
        } catch (Exception ignored) {
            cnpc$changed = false;
            System.out.println("[CustomNPC+] MPM animation hook failed: " + ignored);
            ignored.printStackTrace(System.out);
        }
    }

    @Inject(method = {"render(F)V", "func_78785_a(F)V"}, at = @At("RETURN"), remap = false, require = 1)
    private void cnpc$restoreAnimation(float scale, CallbackInfo callbackInfo) {
        if (!cnpc$changed) return;

        ModelRenderer renderer = (ModelRenderer) (Object) this;
        renderer.rotationPointX = cnpc$pointX;
        renderer.rotationPointY = cnpc$pointY;
        renderer.rotationPointZ = cnpc$pointZ;
        renderer.rotateAngleX = cnpc$angleX;
        renderer.rotateAngleY = cnpc$angleY;
        renderer.rotateAngleZ = cnpc$angleZ;
        renderer.offsetX = cnpc$offsetX;
        renderer.offsetY = cnpc$offsetY;
        renderer.offsetZ = cnpc$offsetZ;
        cnpc$changed = false;
    }
}
