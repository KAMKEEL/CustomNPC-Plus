package noppes.npcs.mixin.late;

import net.minecraft.client.model.ModelBiped;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.client.model.PlayerModelAnimation;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies player animations after Galacticraft calculates its player pose. */
@Pseudo
@Mixin(targets = "micdoodle8.mods.galacticraft.core.client.model.ModelPlayerGC", remap = false)
public abstract class MixinGalacticraftPlayerModel {
    @Unique private PlayerModelAnimation.Pose cnpc$pose;
    @Unique private boolean cnpc$rendering;
    @Unique private static boolean cnpc$loggedHook;

    @Inject(
        method = {
            "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"
        },
        at = @At("HEAD"),
        remap = false,
        require = 1
    )
    private void cnpc$beginRender(Entity entity, float limbSwing, float limbSwingAmount,
                                   float age, float yaw, float pitch, float scale,
                                   CallbackInfo callbackInfo) {
        cnpc$rendering = ClientEventHandler.renderingPlayer == entity
            && ClientEventHandler.renderingPlayerAnimation;
        if (!cnpc$rendering) {
            cnpc$pose = null;
            return;
        }

        ModelBiped model = (ModelBiped) (Object) this;
        cnpc$pose = PlayerModelAnimation.capture(model);
    }

    @Inject(
        method = {
            "setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V",
            "func_78087_a(FFFFFFLnet/minecraft/entity/Entity;)V"
        },
        at = @At("RETURN"),
        remap = false,
        require = 1
    )
    private void cnpc$applyAnimation(float limbSwing, float limbSwingAmount, float age,
                                      float yaw, float pitch, float scale, Entity entity,
                                      CallbackInfo callbackInfo) {
        if (!cnpc$rendering) {
            return;
        }
        ModelBiped model = (ModelBiped) (Object) this;
        PlayerModelAnimation.apply(model);
        if (!cnpc$loggedHook) {
            cnpc$loggedHook = true;
            System.out.println("[CustomNPC+] Galacticraft ModelPlayerGC animation hook is running");
        }
    }

    @Inject(
        method = {
            "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"
        },
        at = @At("RETURN"),
        remap = false,
        require = 1
    )
    private void cnpc$endRender(Entity entity, float limbSwing, float limbSwingAmount,
                                 float age, float yaw, float pitch, float scale,
                                 CallbackInfo callbackInfo) {
        if (cnpc$pose != null) {
            cnpc$pose.restore();
        }
        cnpc$pose = null;
        cnpc$rendering = false;
    }
}
