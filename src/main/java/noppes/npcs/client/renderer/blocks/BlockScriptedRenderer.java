package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
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

    public BlockScriptedRenderer() {
        ((BlockScripted) CustomItems.scripted).renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(this);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        Block blockModel = tile.blockModel;

        if (!overrideModel() && blockModel != null) {
            GL11.glPushMatrix();
            GL11.glRotatef(tile.rotationY, 0, 1, 0);
            GL11.glRotatef(tile.rotationX, 1, 0, 0);
            GL11.glRotatef(tile.rotationZ, 0, 0, 1);
            GL11.glScalef(tile.scaleX, tile.scaleY, tile.scaleZ);
            if (blockModel != CustomItems.scripted) {
                tile.renderFullBlock = renderer.renderBlockByRenderType(blockModel, x, y, z);
            } else {
                renderer.renderStandardBlock(blockModel, x, y, z);
                tile.renderFullBlock = true;
            }
            GL11.glPopMatrix();
        } else {
            renderer.renderStandardBlock(CustomItems.scripted, x, y, z);
            tile.renderFullBlock = true;
        }

        return true;
    }

    public int getRenderId() {
        return CustomItems.scripted.getRenderType();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileScripted tile = (TileScripted) te;

        if (!tile.renderFullBlock) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_BLEND);
            RenderHelper.enableStandardItemLighting();
            GL11.glTranslatef((float) (x + 0.5), (float) y, (float) (z + 0.5));
            GL11.glRotatef(tile.rotationY, 0, 1, 0);
            GL11.glRotatef(tile.rotationX, 1, 0, 0);
            GL11.glRotatef(tile.rotationZ, 0, 0, 1);
            GL11.glScalef(tile.scaleX, tile.scaleY, tile.scaleZ);
            GL11.glTranslatef(0, 0.5F, 0);
            renderItem(te, tile.itemModel);
            GL11.glPopMatrix();
        }

        if (!tile.text1.text.isEmpty()) {
            drawText(tile.text1, x, y, z);
        }
        if (!tile.text2.text.isEmpty()) {
            drawText(tile.text2, x, y, z);
        }
        if (!tile.text3.text.isEmpty()) {
            drawText(tile.text3, x, y, z);
        }
        if (!tile.text4.text.isEmpty()) {
            drawText(tile.text4, x, y, z);
        }
        if (!tile.text5.text.isEmpty()) {
            drawText(tile.text5, x, y, z);
        }
        if (!tile.text6.text.isEmpty()) {
            drawText(tile.text6, x, y, z);
        }
    }

    private void drawText(TextPlane text1, double x, double y, double z) {
        if (text1.textBlock == null || text1.textHasChanged) {
            text1.textBlock = new TextBlockClient(text1.text, 336, true, Minecraft.getMinecraft().thePlayer);
            text1.textHasChanged = false;
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor3f(1, 1, 1);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (x + 0.5), (float) (y + 0.5), (float) (z + 0.5));
        GL11.glRotatef(text1.rotationY, 0, 1, 0);
        GL11.glRotatef(text1.rotationX, 1, 0, 0);
        GL11.glRotatef(text1.rotationZ, 0, 0, 1);
        GL11.glScalef(text1.scale, text1.scale, 1);
        GL11.glTranslatef(text1.offsetX, text1.offsetY, text1.offsetZ);
        float f1 = 0.6666667F;
        float f3 = 0.0133F * f1;
        GL11.glTranslatef(0.0F, 0.5F, 0.01F);
        GL11.glScalef(f3, -f3, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GL11.glDepthMask(false);
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;

        float lineOffset = 0;
        if (text1.textBlock.lines.size() < 14)
            lineOffset = (14f - text1.textBlock.lines.size()) / 2;
        for (int i = 0; i < text1.textBlock.lines.size(); i++) {
            String text = text1.textBlock.lines.get(i).getFormattedText();
            fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, (int) ((lineOffset + i) * (fontrenderer.FONT_HEIGHT - 0.3)), 0);
        }

        GL11.glDepthMask(true);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private void renderItem(TileEntity tile, ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        if (stack != null) {
            mc.renderEngine.bindTexture(stack.getItem() instanceof ItemBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);

            GL11.glScalef(2F, 2F, 2F);
            if (!ForgeHooksClient.renderEntityItem(new EntityItem(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, stack), stack, 0F, 0F, tile.getWorldObj().rand, mc.renderEngine, renderBlocks, 1)) {
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                if (stack.getItem() instanceof ItemBlock && (RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType())
                        || Block.getBlockFromItem(stack.getItem()) instanceof BlockScripted)) {
                    renderBlocks.renderBlockAsItem(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage(), 1F);
                } else {
                    int renderPass = 0;
                    do {
                        IIcon icon = stack.getItem().getIcon(stack, renderPass);
                        if (icon != null) {
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

    private void renderBlock(TileScripted tile, Block b) {
        GL11.glPushMatrix();
        this.bindTexture(TextureMap.locationBlocksTexture);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glTranslatef(-0.5F, -0, 0.5F);
        renderBlocks.renderStandardBlock(b, tile.xCoord, tile.yCoord, tile.zCoord);
        if (b.getTickRandomly() && random.nextInt(12) == 1)
            b.randomDisplayTick(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, random);
        GL11.glPopMatrix();
    }

    public static boolean overrideModel() {
        ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (held == null)
            return false;

        return held.getItem() == CustomItems.wand || held.getItem() == CustomItems.scripter;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }
}
