package noppes.npcs.mixin.late;

import net.minecraft.client.model.ModelBiped;
import noppes.npcs.client.model.PlayerModelAnimation;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/** Applies player animations after PlayerAPI and its model bases calculate the pose. */
@Pseudo
@Mixin(targets = "api.player.model.ModelPlayerAPI", remap = false)
public abstract class MixinPlayerAPIModel {
    @Unique private static Field cnpc$modelField;
    @Unique private static boolean cnpc$modelLookupFailed;
    @Unique private static boolean cnpc$loggedHook;
    @Unique private PlayerModelAnimation.Pose cnpc$pose;
    @Unique private boolean cnpc$rendering;

    @Inject(
        method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
        at = @At("HEAD"),
        remap = false,
        require = 1
    )
    private void cnpc$beginRender(Entity entity, float limbSwing, float limbSwingAmount,
                                   float age, float yaw, float pitch, float scale,
                                   CallbackInfo callbackInfo) {
        ModelBiped model = cnpc$model();
        if (model == null) {
            return;
        }
        cnpc$pose = PlayerModelAnimation.capture(model);
        cnpc$rendering = true;
    }

    @Inject(
        method = "setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V",
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
        ModelBiped model = cnpc$model();
        if (model == null) {
            return;
        }
        PlayerModelAnimation.apply(model);
        if (!cnpc$loggedHook) {
            cnpc$loggedHook = true;
            System.out.println("[CustomNPC+] PlayerAPI model animation hook is running; model="
                + model.getClass().getName());
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
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

    @Unique
    private ModelBiped cnpc$model() {
        if (cnpc$modelLookupFailed) {
            return null;
        }
        try {
            if (cnpc$modelField == null) {
                cnpc$modelField = this.getClass().getDeclaredField("modelPlayer");
                cnpc$modelField.setAccessible(true);
            }
            Object model = cnpc$modelField.get(this);
            return model instanceof ModelBiped ? (ModelBiped) model : null;
        } catch (ReflectiveOperationException exception) {
            cnpc$modelLookupFailed = true;
            System.out.println("[CustomNPC+] PlayerAPI model lookup failed: " + exception);
            return null;
        }
    }
}
