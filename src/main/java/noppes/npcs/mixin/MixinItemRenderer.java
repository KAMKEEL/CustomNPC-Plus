package noppes.npcs.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import noppes.npcs.AnimationMixinFunctions;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemInFirstPerson(float p_78440_1_, CallbackInfo callbackInfo)
    {
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

    @Shadow private RenderBlocks renderBlocksIr;
    @Shadow private static ResourceLocation RES_ITEM_GLINT;
}
