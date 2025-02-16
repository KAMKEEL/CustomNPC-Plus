package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockCouchWool;
import noppes.npcs.blocks.tiles.TileCouchWool;
import noppes.npcs.client.model.blocks.couch.*;
import noppes.npcs.client.model.blocks.legacy.couch.*;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockCouchWoolRenderer extends BlockRendererInterface{

    private final ModelCouchLeft modelCouchLeft = new ModelCouchLeft();
    private final ModelCouchRight modelCouchRight = new ModelCouchRight();
    private final ModelCouchCorner modelCouchCorner = new ModelCouchCorner();
    public static  final ModelCouchMiddle modelCouch = new ModelCouchMiddle();
    private final ModelCouchSingle modelCouchSingle = new ModelCouchSingle();

    public static final ModelBase modelLegacyCouchMiddle = new ModelLegacyCouchMiddle();
	public static final ModelBase modelLegacyCouchMiddleWool = new ModelLegacyCouchMiddleWool();

	private final ModelBase modelLegacyCouchLeft = new ModelLegacyCouchLeft();
	private final ModelBase modelLegacyCouchLeftWool = new ModelLegacyCouchLeftWool();

	private final ModelBase modelLegacyCouchRight = new ModelLegacyCouchRight();
	private final ModelBase modelLegacyCouchRightWool = new ModelLegacyCouchRightWool();

	private final ModelBase modelLegacyCouchCorner = new ModelLegacyCouchCorner();
	private final ModelBase modelLegacyCouchCornerWool = new ModelLegacyCouchCornerWool();


    public static final ResourceLocation oak = new ResourceLocation("customnpcs","textures/models/couch/oak.png");
    public static final ResourceLocation spruce = new ResourceLocation("customnpcs","textures/models/couch/spruce.png");
    public static final ResourceLocation birch = new ResourceLocation("customnpcs","textures/models/couch/birch.png");
    public static final ResourceLocation jungle = new ResourceLocation("customnpcs","textures/models/couch/jungle.png");
    public static final ResourceLocation acacia = new ResourceLocation("customnpcs","textures/models/couch/acacia.png");
    public static final ResourceLocation dark_oak = new ResourceLocation("customnpcs","textures/models/couch/dark_oak.png");

    public static final ResourceLocation wool = new ResourceLocation("customnpcs","textures/models/couch/wool.png");

    public BlockCouchWoolRenderer(){
		((BlockCouchWool)CustomItems.couchWool).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileCouchWool tile = (TileCouchWool) var1;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glS calef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        if(ConfigClient.LegacyCouch){
            setWoodTexture(var1.getBlockMetadata());
            if(tile.hasCornerLeft)
                modelLegacyCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasCornerRight){
                GL11.glRotatef(90, 0, 1, 0);
                modelLegacyCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
            else if(tile.hasLeft && tile.hasRight)
                modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft)
                modelLegacyCouchLeft.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasRight)
                modelLegacyCouchRight.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(BlockTallLampRenderer.resourceTop);
            float[] color =  ColorUtil.hexToRGB(tile.color);
            GL11.glColor3f(color[0], color[1], color[2]);

            if(tile.hasCornerLeft || tile.hasCornerRight)
                modelLegacyCouchCornerWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft && tile.hasRight)
                modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft)
                modelLegacyCouchLeftWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasRight)
                modelLegacyCouchRightWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setCouchWood(var1.getBlockMetadata());
            if(tile.hasCornerLeft){
                GL11.glPushMatrix();
                GL11.glRotatef(90, 0, 1, 0);
                modelCouchCorner.CouchBack.render(0.0625F);
                GL11.glPopMatrix();
            }
            else if(tile.hasCornerRight)
                modelCouchCorner.CouchBack.render(0.0625F);
            else if(tile.hasLeft && tile.hasRight)
                modelCouch.CouchBack.render(0.0625F);
            else if(tile.hasLeft)
                modelCouchLeft.CouchBack.render(0.0625F);
            else if(tile.hasRight)
                modelCouchRight.CouchBack.render(0.0625F);
            else {
                modelCouchSingle.CouchBack.render(0.0625F);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(wool);
            float[] color =  ColorUtil.hexToRGB(tile.color);
            GL11.glColor3f(color[0], color[1], color[2]);

            if(tile.hasCornerLeft)
                modelCouchCorner.Cussion.render(0.0625F);
            else if(tile.hasCornerRight)
                modelCouchCorner.Cussion.render(0.0625F);
            else if(tile.hasLeft && tile.hasRight)
                modelCouch.Cussion.render(0.0625F);
            else if(tile.hasLeft)
                modelCouchLeft.Cussion.render(0.0625F);
            else if(tile.hasRight)
                modelCouchRight.Cussion.render(0.0625F);
            else {
                modelCouchSingle.Cussion.render(0.0625F);
            }
        }
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

    public void setCouchWood(int meta){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(meta == 1)
            manager.bindTexture(spruce);
        else if(meta == 2)
            manager.bindTexture(birch);
        else if(meta == 3)
            manager.bindTexture(jungle);
        else if(meta == 4)
            manager.bindTexture(acacia);
        else if(meta == 5)
            manager.bindTexture(dark_oak);
        else
            manager.bindTexture(oak);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
	public int getRenderId() {
		return CustomItems.couchWool.getRenderType();
	}
}
