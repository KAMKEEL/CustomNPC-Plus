package noppes.npcs.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.AnimationMixinFunctions;
import noppes.npcs.client.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemInFirstPerson(float p_78440_1_, CallbackInfo callbackInfo) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Render renderer = RenderManager.instance.getEntityRenderObject(player);
        if (renderer instanceof RendererLivingEntity && ((RendererLivingEntity) renderer).mainModel instanceof ModelBiped) {
            Render playerRenderer = RenderManager.instance.getEntityRenderObject(player);
            if (playerRenderer instanceof RendererLivingEntity) {
                ClientEventHandler.renderer = (RendererLivingEntity) playerRenderer;
            }
            ClientEventHandler.partialRenderTick = Minecraft.getMinecraft().timer.renderPartialTicks;
            ClientEventHandler.partialHandTicks = p_78440_1_;

            ClientEventHandler.renderingPlayer = player;
            ClientEventHandler.firstPersonAnimation = true;
            ClientEventHandler.firstPersonModel = (ModelBiped) ((RendererLivingEntity) renderer).mainModel;
            if (AnimationMixinFunctions.mixin_renderFirstPersonAnimation(p_78440_1_, player, ClientEventHandler.firstPersonModel, renderBlocksIr, RES_ITEM_GLINT)) {
                callbackInfo.cancel();
            }
            ClientEventHandler.firstPersonAnimation = false;
            ClientEventHandler.renderingPlayer = null;
        }
    }

    @Shadow
    private RenderBlocks renderBlocksIr;
    @Shadow
    private static ResourceLocation RES_ITEM_GLINT;
}
