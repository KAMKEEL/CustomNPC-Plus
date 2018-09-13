package noppes.npcs.items;

import org.lwjgl.opengl.GL11;


public class ItemBattleAxe extends ItemNpcWeaponInterface{

	public ItemBattleAxe(int par1, ToolMaterial tool) {
		super(par1, tool);
	}

	public void renderSpecial(){
        GL11.glScalef(1f, 0.8f,1f);
        GL11.glTranslatef(-0.04f, 0.2f, -0.16f);
        GL11.glRotatef(180, 0, 1, 0);
	}
}
