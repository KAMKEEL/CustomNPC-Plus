package noppes.npcs.client.renderer.items;

import kamkeel.npcs.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import noppes.npcs.client.renderer.blocks.BlockCouchWoolRenderer;
import noppes.npcs.client.renderer.blocks.BlockRendererInterface;
import noppes.npcs.client.renderer.blocks.BlockTallLampRenderer;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;

public class ItemCouchWoolRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        int meta = item.getItemDamage();
        int colorValue = ColorUtil.colorTableInts[15 - meta];
        if (item.hasTagCompound() && item.getTagCompound().hasKey("BrushColor")) {
            colorValue = item.getTagCompound().getInteger("BrushColor");
        }
        float[] color = ColorUtil.hexToRGB(colorValue);

        Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glPushMatrix();
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON){
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }

        GL11.glTranslatef(0, 0.9f, 0.1f);
        GL11.glScalef(0.9f, 0.9f, 0.9f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if (ConfigClient.LegacyCouch) {
            setWoodTexture(meta);
            GL11.glColor3f(1, 1, 1);
            BlockCouchWoolRenderer.modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            // Bind the top texture (for the wool overlay)
            mc.getTextureManager().bindTexture(BlockTallLampRenderer.resourceTop);
            GL11.glColor3f(color[0], color[1], color[2]);
            BlockCouchWoolRenderer.modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setCouchWood(meta);
            GL11.glColor3f(1, 1, 1);
            // Render the main couch model (assumes modelCouch has parts CouchBack and Cussion)
            BlockCouchWoolRenderer.modelCouch.CouchBack.render(0.0625F);
            mc.getTextureManager().bindTexture(BlockCouchWoolRenderer.wool);
            GL11.glColor3f(color[0], color[1], color[2]);
            BlockCouchWoolRenderer. modelCouch.Cussion.render(0.0625F);
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();

        GL11.glPopMatrix();
    }

    private void setCouchWood(int meta) {
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if (meta == 1)
            manager.bindTexture(BlockCouchWoolRenderer.spruce);
        else if (meta == 2)
            manager.bindTexture(BlockCouchWoolRenderer.birch);
        else if (meta == 3)
            manager.bindTexture(BlockCouchWoolRenderer.jungle);
        else if (meta == 4)
            manager.bindTexture(BlockCouchWoolRenderer.acacia);
        else if (meta == 5)
            manager.bindTexture(BlockCouchWoolRenderer.dark_oak);
        else
            manager.bindTexture(BlockCouchWoolRenderer.oak);
    }

    private void setWoodTexture(int meta) {
        BlockRendererInterface.setWoodTexture(meta);
    }
}
