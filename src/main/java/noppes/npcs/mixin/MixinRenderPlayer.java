package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
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
        if (ClientCacheHandler.playerAnimations.containsKey(p_77039_1_.getUniqueID())) {
            AnimationData animData = ClientCacheHandler.playerAnimations.get(p_77039_1_.getUniqueID());
            if (animData != null && animData.isActive()) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                    FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                    part.interpolateOffset();
                    part.interpolateAngles();
                    float pi = 180 / (float) Math.PI;
                    GL11.glTranslatef(part.prevPivots[0], part.prevPivots[1], part.prevPivots[2]);
                    GL11.glRotatef(part.prevRotations[0] * pi, 1, 0, 0);
                    GL11.glRotatef(part.prevRotations[1] * pi, 0, 1, 0);
                    GL11.glRotatef(part.prevRotations[2] * pi, 0, 0, 1);
                }
            }
        }
    }
}
