package noppes.npcs.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import noppes.npcs.CustomItems;

import org.lwjgl.opengl.GL11;

public class ItemKunaiReversed extends ItemKunai{

	public ItemKunaiReversed(int par1, ToolMaterial tool) {
		super(par1, tool);

	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.4f, 0.4f, 0.4f);
        GL11.glRotatef(180F, 1F, 0F, 0F);
        
        GL11.glTranslatef(-0.4f, -0.9f, 0.2f);
	}
	@Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
    	this.itemIcon = CustomItems.kunai.getIconFromDamage(0);
    }
}
