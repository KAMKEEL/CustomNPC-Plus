package noppes.npcs.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @Inject(method = "mountEntity", at = @At("HEAD"), cancellable = true)
    private void customnpcs$restrictMountDismount(Entity entity, CallbackInfo ci) {
        if (entity != null) {
            return;
        }
        Entity mount = ((Entity) (Object) this).ridingEntity;
        if (mount instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) mount;
            if (!npc.canMountRiderDismount((Entity) (Object) this)) {
                ci.cancel();
            }
        }
    }
}
