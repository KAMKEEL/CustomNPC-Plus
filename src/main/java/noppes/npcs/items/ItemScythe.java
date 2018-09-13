package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

public class ItemScythe extends ItemNpcWeaponInterface{

	public ItemScythe(int par1 ,ToolMaterial tool) {
		super(par1, tool);
	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(1f, 1.3f, 1f);
        GL11.glTranslatef(0.0F, -0.2f, -0.16f);
        GL11.glRotatef(180, 0, 1, 0);
	}
}
