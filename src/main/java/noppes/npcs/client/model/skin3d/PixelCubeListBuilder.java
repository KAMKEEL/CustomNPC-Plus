package noppes.npcs.client.model.skin3d;

import java.util.ArrayList;
import java.util.List;

public class PixelCubeListBuilder {
    private int texU;
    private int texV;
    private int texWidth = 64;
    private int texHeight = 64;
    private boolean mirror;
    private final List<PixelCube> cubes = new ArrayList<PixelCube>();

    public static PixelCubeListBuilder create() {
        return new PixelCubeListBuilder();
    }

    public PixelCubeListBuilder texOffs(int u, int v) {
        this.texU = u;
        this.texV = v;
        return this;
    }

    public PixelCubeListBuilder mirror(boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    public PixelCubeListBuilder texSize(int width, int height){
        this.texWidth = width;
        this.texHeight = height;
        return this;
    }

    public PixelCubeListBuilder addBox(float x, float y, float z, float size, FaceDirection[] hide) {
        cubes.add(new PixelCube(texU, texV, x, y, z, size, mirror, texWidth, texHeight, hide));
        return this;
    }

    public List<PixelCube> getCubes() {
        return cubes;
    }
}
