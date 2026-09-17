package noppes.npcs.mixin;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.AnimationMixinFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies CustomNPC+ animation values after MPM calculates its ordinary pose
 * and immediately before MPM draws its replacement player model.
 */
@Pseudo
@Mixin(targets = "noppes.mpm.client.model.ModelMPM", remap = false)
public abstract class MixinMPMModel {

    @Unique private static boolean cnpc$loggedModelEntry;
    @Unique private static boolean cnpc$loggedModelHook;

    @Inject(
        method = {
            "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"
        },
        at = @At("HEAD"),
        remap = false,
        require = 1
    )
    private void cnpc$logModelEntry(Entity entity, float limbSwing, float limbSwingAmount,
                                     float age, float yaw, float pitch, float scale,
                                     CallbackInfo callbackInfo) {
        if (!cnpc$loggedModelEntry) {
            cnpc$loggedModelEntry = true;
            System.out.println("[CustomNPC+] MPM ModelMPM render method is running");
        }
    }

    @Inject(
        method = {
            "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnoppes/mpm/client/model/ModelMPM;renderHead(Lnet/minecraft/entity/Entity;F)V",
            shift = At.Shift.BEFORE,
            remap = false
        ),
        remap = false,
        require = 1
    )
    private void cnpc$applyPlayerAnimation(Entity entity, float limbSwing, float limbSwingAmount,
                                            float age, float yaw, float pitch, float scale,
                                            CallbackInfo callbackInfo) {
        ModelBiped model = (ModelBiped) (Object) this;
        if (!cnpc$loggedModelHook) {
            cnpc$loggedModelHook = true;
            System.out.println("[CustomNPC+] MPM ModelMPM pose hook is running; arm="
                + model.bipedRightArm.getClass().getName());
        }

        ModelRenderer[] parts = new ModelRenderer[]{
            model.bipedHead,
            model.bipedBody,
            model.bipedRightArm,
            model.bipedLeftArm,
            model.bipedRightLeg,
            model.bipedLeftLeg
        };
        for (ModelRenderer part : parts) {
            AnimationMixinFunctions.applyValues(part);
        }
    }
}
