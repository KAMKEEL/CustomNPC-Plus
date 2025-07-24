package noppes.npcs.client.model.skin3d;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;

public class PixelModelPart {
    private final List<PixelCube> cubes;
    public boolean visible = true;

    public PixelModelPart(List<PixelCube> cubes) {
        this.cubes = cubes;
    }

    public void render() {
        if(!visible) return;
        Tessellator tess = Tessellator.instance;
        for(PixelCube cube : cubes) {
            cube.render(tess);
        }
    }
}
