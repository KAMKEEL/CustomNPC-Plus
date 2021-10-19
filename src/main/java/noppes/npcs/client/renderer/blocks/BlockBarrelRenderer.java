package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockBarrel;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.ModelBarrel;
import noppes.npcs.client.model.blocks.ModelBarrelLit;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockBarrelRenderer extends BlockRendererInterface {

    private static final ResourceLocation resource1 = new ResourceLocation("customnpcs", "textures/models/Barrel.png");
    private final ModelBarrel model = new ModelBarrel();
    private final ModelBarrelLit modelLit = new ModelBarrelLit();

    public BlockBarrelRenderer() {
        ((BlockBarrel) CustomItems.barrel).renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(this);
    }

    @Override
    public void renderTileEntityAt(TileEntity var1, double var2, double var4,
                                   double var6, float var8) {
        TileColorable tile = (TileColorable) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) var2 + 0.5f, (float) var4 + 1.42f, (float) var6 + 0.5f);
        GL11.glScalef(1f, 0.94f, 1f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        GL11.glEnable(GL11.GL_CULL_FACE);
        setWoodTexture(var1.getBlockMetadata());
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resource1);
        modelLit.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId,
                                     RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.75f, 0);
        GL11.glScalef(0.7f, 0.7f, 0.7f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setWoodTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(resource1);
        modelLit.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        GL11.glPopMatrix();
    }

    @Override
    public int getRenderId() {
        return CustomItems.barrel.getRenderType();
    }
}
