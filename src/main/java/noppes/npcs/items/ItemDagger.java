package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

public class ItemDagger extends ItemNpcWeaponInterface{

	public ItemDagger(int par1, ToolMaterial tool) {
		super(par1, tool);
	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.6f, 0.6f, 0.6f);
        GL11.glTranslatef(0.14F, 0.22f, 0.06f);
	}
}
