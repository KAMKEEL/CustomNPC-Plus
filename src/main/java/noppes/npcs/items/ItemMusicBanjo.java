package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

public class ItemMusicBanjo extends ItemMusic{

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.85f, 0.85f,0.85f);
        GL11.glTranslatef(0.1f, 0.4f, -0.14f);
        GL11.glRotatef(-90, -1F, 0, 0F);
	}
}
