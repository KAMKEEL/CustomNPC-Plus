package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.controllers.data.PlayerModelData;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderPlayer.class)
public class MixinRenderPlayer {

    @SideOnly(Side.CLIENT)
    @Inject(method = "renderLivingAt", at = @At(value = "TAIL"))
    protected void modelDataRotations(AbstractClientPlayer p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_, CallbackInfo callbackInfo)
    {
        if (Client.playerModelData.containsKey(p_77039_1_.getUniqueID())) {
            PlayerModelData data = Client.playerModelData.get(p_77039_1_.getUniqueID());
            if (data.enabled()) {
                this.setInterpolatedAngles(data);
                if (data.rotationEnabledX) {
                    GL11.glRotatef(data.modelRotations[0], 1, 0, 0);
                }
                if (data.rotationEnabledY) {
                    GL11.glRotatef(data.modelRotations[1], 0, 1, 0);
                }
                if (data.rotationEnabledZ) {
                    GL11.glRotatef(data.modelRotations[2], 0, 0, 1);
                }
            }
        }
    }

    public void setInterpolatedAngles(PlayerModelData modelData) {
        float pi = (float) Math.PI * (modelData.fullAngles ? 2 : 1);
        if (!modelData.animate) {
            modelData.modelRotations[0] = modelData.rotationX * pi;
            modelData.modelRotations[1] = modelData.rotationY * pi;
            modelData.modelRotations[2] = modelData.rotationZ * pi;
        } else if (modelData.modelRotPartialTicks != ClientEventHandler.partialRenderTick) {
            modelData.modelRotPartialTicks = ClientEventHandler.partialRenderTick;
            if (modelData.rotationX - modelData.modelRotations[0] != 0 && modelData.rotationEnabledX) {
                modelData.modelRotations[0] = (modelData.rotationX - modelData.modelRotations[0]) * modelData.animRate / 10f + modelData.modelRotations[0];
            } else {
                modelData.modelRotations[0] = modelData.rotationX;
            }

            if (modelData.rotationY - modelData.modelRotations[1] != 0 && modelData.rotationEnabledY) {
                modelData.modelRotations[1] = (modelData.rotationY - modelData.modelRotations[1]) * modelData.animRate / 10f + modelData.modelRotations[1];
            } else {
                modelData.modelRotations[1] = modelData.rotationY;
            }

            if (modelData.rotationZ - modelData.modelRotations[2] != 0 && modelData.rotationEnabledZ) {
                modelData.modelRotations[2] = (modelData.rotationZ - modelData.modelRotations[2]) * modelData.animRate /10f + modelData.modelRotations[2];
            } else {
                modelData.modelRotations[2] = modelData.rotationZ;
            }
        }
    }
}
