package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockWeaponRack;
import noppes.npcs.blocks.tiles.TileWeaponRack;
import noppes.npcs.client.model.blocks.ModelWeaponRack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockWeaponRackRenderer extends BlockRendererInterface{

	private final ModelWeaponRack model = new ModelWeaponRack();
	
	public BlockWeaponRackRenderer(){
		((BlockWeaponRack)CustomItems.weaponsRack).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileWeaponRack tile = (TileWeaponRack) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.34f, (float)var6 + 0.5f);
        GL11.glScalef(0.9f, 0.9f, 0.9f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        setWoodTexture(var1.getBlockMetadata());
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        if(!this.playerTooFar(tile)){
	        for(int i = 0; i < 3; i++)
	        	doRender(tile.getStackInSlot(i), i);
        }
		GL11.glPopMatrix();
	}
	
	private void doRender(ItemStack item, int pos){
		if(item == null || item.getItem() == null || item.getItem() instanceof ItemBlock)
			return;
        GL11.glPushMatrix();
		GL11.glTranslatef(-0.40f + pos * 0.37f, 0.8f, 0.23f);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        if(item.getItem().shouldRotateAroundWhenRendering()){
        	GL11.glTranslatef(0.14f, 0, 0.5f);
        	GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        }
        else
        	GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-200.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);

        if (item.getItem().requiresMultipleRenderPasses())
        {
            for (int k = 0; k <= item.getItem().getRenderPasses(item.getItemDamage()); ++k)
            {
                int i = item.getItem().getColorFromItemStack(item, k);
                float f12 = (float)(i >> 16 & 255) / 255.0F;
                float f4 = (float)(i >> 8 & 255) / 255.0F;
                float f5 = (float)(i & 255) / 255.0F;
                GL11.glColor4f(f12, f4, f5, 1.0F);
                RenderManager.instance.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer, item, k);
            }
        }
        else
        {
            int k = item.getItem().getColorFromItemStack(item, 0);
            float f11 = (float)(k >> 16 & 255) / 255.0F;
            float f12 = (float)(k >> 8 & 255) / 255.0F;
            float f4 = (float)(k & 255) / 255.0F;
            GL11.glColor4f(f11, f12, f4, 1.0F);
            RenderManager.instance.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer, item, 0);
        }
		GL11.glPopMatrix();
		
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.3f, 0.15f, 0);
        GL11.glScalef(0.9f, 0.7f, 0.9f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setWoodTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.weaponsRack.getRenderType();
	}
	
	public int specialRenderDistance(){
		return 26;
	}
}
