package noppes.npcs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientEventHandler;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "renderWorld", at = @At(value = "RETURN"))
    private void renderFirstPersonOverlays(float p_78476_1_, long p_78476_2_, CallbackInfo callbackInfo)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (ClientEventHandler.hasOverlays(player)) {
            GL11.glPushMatrix();
            ClientEventHandler.renderCNPCPlayer.renderHand(p_78476_1_,0);
            GL11.glPopMatrix();
        }
    }
}
