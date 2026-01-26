package kamkeel.npcs.client.renderer.quadric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

/**
 * Renders a sphere using GLU quadrics.
 *
 * Design inspired by LouisXIV's quadric rendering system.
 */
@SideOnly(Side.CLIENT)
public class SphereRenderer extends QuadricRenderer {

    public SphereRenderer(float radius, int slices, int stacks) {
        super(radius, slices, stacks);
    }

    public SphereRenderer(float radius, int resolution) {
        super(radius, resolution);
    }

    public SphereRenderer() {
        super();
    }

    @Override
    protected void compile() {
        Sphere sphere = new Sphere();
        sphere.setTextureFlag(true);  // Enable texture coordinates
        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GLU.GLU_NONE);  // No normals for self-illuminated rendering
        sphere.setOrientation(GLU.GLU_OUTSIDE);
        sphere.draw(radius, slices, stacks);
    }
}
