package noppes.npcs.client.model.skin3d;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager;

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
        GlStateManager.pushMatrix();
        GlStateManager.translate(x/16f, y/16f, z/16f);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        for(PixelCube cube : cubes) {
            cube.render(tess);
        }
        tess.draw();
        GlStateManager.popMatrix();
    }
}
