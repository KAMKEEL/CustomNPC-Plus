package noppes.npcs.client.model.skin3d;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class PixelModelPart {
    private final List<PixelCube> cubes;
    public float x;
    public float y;
    public float z;
    public boolean visible = true;

    public PixelModelPart(List<PixelCube> cubes) {
        this.cubes = cubes;
    }

    public void render() {
        if(!visible) return;
        GL11.glPushMatrix();
        GL11.glTranslatef(x/16f, y/16f, z/16f);
        Tessellator tess = Tessellator.instance;
        for(PixelCube cube : cubes) {
            cube.render(tess);
        }
        GL11.glPopMatrix();
    }
}
