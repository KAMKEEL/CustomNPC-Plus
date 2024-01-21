package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.geckominecraft.client.renderer.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.ForgeHooksClient;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScripted.TextPlane;
import noppes.npcs.client.TextBlockClient;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;

public class BlockScriptedRenderer extends BlockRendererInterface{
    private static final RenderBlocks renderBlocks = new RenderBlocks();
    private static final Random random = new Random();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileScripted tile = (TileScripted) te;
        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        if(overrideModel()){
            GlStateManager.translate(0, 0.5, 0);
            renderItem(te, new ItemStack(CustomItems.scripted));
        }
        else{
            GlStateManager.rotate(tile.rotationY, 0, 1, 0);
            GlStateManager.rotate(tile.rotationX, 1, 0, 0);
            GlStateManager.rotate(tile.rotationZ, 0, 0, 1);
            GlStateManager.scale(tile.scaleX, tile.scaleY, tile.scaleZ);
            Block b = tile.blockModel;
            if(b == null || b == Blocks.air){
                GlStateManager.translate(0, 0.5, 0);
                renderItem(te, tile.itemModel);
            }
            else if(b == CustomItems.scripted){
                GlStateManager.translate(0, 0.5, 0);
                renderItem(te, tile.itemModel);
            }
            else{
                int meta = tile.itemModel.getItemDamage();
                renderBlock(tile, b);

                if(b.hasTileEntity(meta) && !tile.renderTileErrored){
                    try{
                        if(tile.renderTile == null){
                            TileEntity entity = b.createTileEntity(te.getWorldObj(), meta);
                            entity.zCoord=tile.zCoord;
                            entity.yCoord=tile.yCoord;
                            entity.xCoord=tile.xCoord;
                            entity.setWorldObj(te.getWorldObj());
                            ObfuscationReflectionHelper.setPrivateValue(TileEntity.class, entity, tile.itemModel.getItemDamage(), 5);
                            ObfuscationReflectionHelper.setPrivateValue(TileEntity.class, entity, b, 6);
                            tile.renderTile = entity;
                            if(entity instanceof ITickable){
                                tile.renderTileUpdate = (ITickable) entity;
                            }
                        }
                        TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile.renderTile);

                        if(renderer != null){
                            renderer.renderTileEntityAt(tile.renderTile, -0.5, 0, -0.5, partialTicks);

                        }
                        else
                            tile.renderTileErrored = true;
                    }
                    catch(Exception e){
                        tile.renderTileErrored = true;
                    }
                }
            }
        }
        GlStateManager.popMatrix();

        if(!tile.text1.text.isEmpty()) {
            drawText(tile.text1, x, y, z);
        }
        if(!tile.text2.text.isEmpty()) {
            drawText(tile.text2, x, y, z);
        }
        if(!tile.text3.text.isEmpty()) {
            drawText(tile.text3, x, y, z);
        }
        if(!tile.text4.text.isEmpty()) {
            drawText(tile.text4, x, y, z);
        }
        if(!tile.text5.text.isEmpty()) {
            drawText(tile.text5, x, y, z);
        }
        if(!tile.text6.text.isEmpty()) {
            drawText(tile.text6, x, y, z);
        }
    }

    private void drawText(TextPlane text1, double x, double y, double z) {
        if(text1.textBlock == null || text1.textHasChanged){
            text1.textBlock = new TextBlockClient(text1.text, 336, true, Minecraft.getMinecraft().thePlayer);
            text1.textHasChanged = false;
        }
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1, 1, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.rotate(text1.rotationY, 0, 1, 0);
        GlStateManager.rotate(text1.rotationX, 1, 0, 0);
        GlStateManager.rotate(text1.rotationZ, 0, 0, 1);
        GlStateManager.scale(text1.scale, text1.scale, 1);
        GlStateManager.translate(text1.offsetX, text1.offsetY, text1.offsetZ);
        float f1 = 0.6666667F;
        float f3 = 0.0133F * f1;
        GlStateManager.translate(0.0F, 0.5f, 0.01F);
        GlStateManager.scale(f3, -f3, f3);
        GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GlStateManager.depthMask(false);
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;

        float lineOffset = 0;
        if(text1.textBlock.lines.size() < 14)
            lineOffset = (14f - text1.textBlock.lines.size()) / 2;
        for(int i = 0; i < text1.textBlock.lines.size(); i++){
            String text = text1.textBlock.lines.get(i).getFormattedText();
            fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, (int)((lineOffset + i) * (fontrenderer.FONT_HEIGHT - 0.3)), 0);
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void renderItem(TileEntity tile, ItemStack stack){
        Minecraft mc = Minecraft.getMinecraft();
        if(stack != null) {
            mc.renderEngine.bindTexture(stack.getItem() instanceof ItemBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);

            GL11.glScalef(2F, 2F, 2F);
            if(!ForgeHooksClient.renderEntityItem(new EntityItem(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, stack), stack, 0F, 0F, tile.getWorldObj().rand, mc.renderEngine, renderBlocks, 1)) {
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                if(stack.getItem() instanceof ItemBlock && (RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType())
                        || Block.getBlockFromItem(stack.getItem()) instanceof BlockScripted)) {
                    renderBlocks.renderBlockAsItem(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage(), 1F);
                } else {
                    int renderPass = 0;
                    do {
                        IIcon icon = stack.getItem().getIcon(stack, renderPass);
                        if(icon != null) {
                            Color color = new Color(stack.getItem().getColorFromItemStack(stack, renderPass));
                            GL11.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                            float f = icon.getMinU();
                            float f1 = icon.getMaxU();
                            float f2 = icon.getMinV();
                            float f3 = icon.getMaxV();
                            GL11.glTranslatef(-0.5F, -0.5F, 0F);
                            ItemRenderer.renderItemIn2D(Tessellator.instance, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 1F / 16F);
                            GL11.glColor3f(1F, 1F, 1F);
                        }
                        renderPass++;
                    } while(renderPass < stack.getItem().getRenderPasses(stack.getItemDamage()));
                }
            }
        }
    }

    private void renderBlock(TileScripted tile, Block b){
        GlStateManager.pushMatrix();
        this.bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.translate(-0.5F, -0, 0.5F);
        RenderBlocks.getInstance().renderStandardBlock(b, tile.xCoord, tile.yCoord, tile.zCoord);
        if(b.getTickRandomly() && random.nextInt(12) == 1)
            b.randomDisplayTick(tile.getWorldObj(), tile.xCoord,tile.yCoord,tile.zCoord, random);
        GlStateManager.popMatrix();
    }

    private boolean overrideModel(){
        ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
        if(held == null)
            return false;

        return held.getItem() == CustomItems.wand || held.getItem() == CustomItems.scripter;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        //TODO
    }

    @Override
    public int getRenderId() {
        return 0;
    }
}