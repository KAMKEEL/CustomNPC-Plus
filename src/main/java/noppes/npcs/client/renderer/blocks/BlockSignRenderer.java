package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockRotated;
import noppes.npcs.blocks.tiles.TileSign;
import noppes.npcs.client.model.blocks.ModelSign;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockSignRenderer extends BlockRendererInterface{

	private final ModelSign model = new ModelSign();
    
	public BlockSignRenderer(){
		((BlockRotated)CustomItems.sign).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileSign tile = (TileSign) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.62f, (float)var6 + 0.5f);
        //GL11.glScalef(1.2f, 1.1f, 1.2f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation + 90, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(Steel);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        setWoodTexture(tile.getBlockMetadata());
        model.Sign.render(0.0625F);
        
        if(tile.icon != null && !this.playerTooFar(tile)){
        	doRender(var2, var4, var6, tile.rotation, tile.icon);
        }
        
		GL11.glPopMatrix();
	}
	
    public void doRender(double par2, double par4, double par6, int meta, ItemStack iicon)
    {
        GL11.glPushMatrix();
        bindTexture(TextureMap.locationItemsTexture);
        GL11.glTranslatef(0, 1.02f, -0.03f);
        GL11.glDepthMask(false);
        float f2 = 0.024f;
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glScalef(f2, f2, f2);
        renderItemBanner(mc.renderEngine, iicon, -8, -8, false);
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glTranslatef(0, 0, -2.9f);
        renderItemBanner(mc.renderEngine, iicon, -8, -8, false);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }


    public void renderItemBanner(TextureManager txtMng, ItemStack item, int p_77015_4_, int p_77015_5_, boolean renderEffect)
    {
        Object object = item.getIconIndex();
        int l;
        float f;
        float f3;
        float f4;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        ResourceLocation resourcelocation = txtMng.getResourceLocation(item.getItemSpriteNumber());
        txtMng.bindTexture(resourcelocation);

        if (object == null)
        {
            object = ((TextureMap)Minecraft.getMinecraft().getTextureManager().getTexture(resourcelocation)).getAtlasSprite("missingno");
        }

        l = item.getItem().getColorFromItemStack(item, 0);
        f3 = (float)(l >> 16 & 255) / 255.0F;
        f4 = (float)(l >> 8 & 255) / 255.0F;
        f = (float)(l & 255) / 255.0F;

        if (renderer.renderWithColor)
        {
            GL11.glColor4f(f3, f4, f, 1.0F);
        }

        GL11.glDisable(GL11.GL_LIGHTING); //Forge: Make sure that render states are reset, a renderEffect can derp them up.
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        renderer.renderIcon(p_77015_4_, p_77015_5_, (IIcon)object, 16, 16);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        if (renderEffect && item.hasEffect(0))
        {
            renderer.renderEffect(txtMng, p_77015_4_, p_77015_5_);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.6f, 0);
        GL11.glScalef(1f, 1f, 1f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        Minecraft.getMinecraft().getTextureManager().bindTexture(Steel);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        setWoodTexture(metadata);
        model.Sign.render(0.0625F);
        
		GL11.glPopMatrix();
	}



	@Override
	public int getRenderId() {
		return CustomItems.sign.getRenderType();
	}
}
