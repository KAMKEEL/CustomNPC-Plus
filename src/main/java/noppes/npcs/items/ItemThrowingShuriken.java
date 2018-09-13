package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

public class ItemThrowingShuriken extends ItemThrowingWeapon{

	public ItemThrowingShuriken(int par1) {
		super(par1);
	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glTranslatef(-0.1F, 0.3f, 0f);
	}
	@Override
	public boolean shouldRotateAroundWhenRendering(){
		return true;
	}
}
