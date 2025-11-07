package noppes.npcs.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleMount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void customnpcs$blockMountSprint(boolean sprinting, CallbackInfo ci) {
        if (!sprinting) {
            return;
        }
        if (!(((Object) this) instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) (Object) this;
        Entity mount = player.ridingEntity;
        if (mount instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) mount;
            if (npc.advanced.role == EnumRoleType.Mount && npc.roleInterface instanceof RoleMount) {
                RoleMount roleMount = (RoleMount) npc.roleInterface;
                if (!roleMount.isSprintAllowed()) {
                    ci.cancel();
                }
            }
        }
    }
}
