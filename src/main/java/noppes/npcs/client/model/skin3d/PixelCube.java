package noppes.npcs.client.model.skin3d;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.model.PositionTextureVertex;
import java.util.EnumSet;

public class PixelCube {
    private final float minX, minY, minZ;
    private final float maxX, maxY, maxZ;
    private final EnumSet<FaceDirection> hidden;
    private final TexturedQuad[] quads = new TexturedQuad[6];

    public PixelCube(int u, int v, float x, float y, float z, float size, boolean mirror, int texW, int texH, FaceDirection[] hide) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + size;
        this.maxY = y + size;
        this.maxZ = z + size;
        this.hidden = EnumSet.noneOf(FaceDirection.class);
        if(hide != null) {
            for(FaceDirection d : hide) hidden.add(d);
        }
        float px = mirror ? maxX : minX;
        float px2 = mirror ? minX : maxX;
        PositionTextureVertex v1 = new PositionTextureVertex(px, minY, minZ, (u)/texW, (v)/texH);
        PositionTextureVertex v2 = new PositionTextureVertex(px2, minY, minZ, (u+size)/texW, (v)/texH);
        PositionTextureVertex v3 = new PositionTextureVertex(px2, maxY, minZ, (u+size)/texW, (v+size)/texH);
        PositionTextureVertex v4 = new PositionTextureVertex(px, maxY, minZ, (u)/texW, (v+size)/texH);
        PositionTextureVertex v5 = new PositionTextureVertex(px, minY, maxZ, (u)/texW, (v)/texH);
        PositionTextureVertex v6 = new PositionTextureVertex(px2, minY, maxZ, (u+size)/texW, (v)/texH);
        PositionTextureVertex v7 = new PositionTextureVertex(px2, maxY, maxZ, (u+size)/texW, (v+size)/texH);
        PositionTextureVertex v8 = new PositionTextureVertex(px, maxY, maxZ, (u)/texW, (v+size)/texH);

        quads[0] = new TexturedQuad(new PositionTextureVertex[]{v6,v5,v1,v2}); //down
        quads[1] = new TexturedQuad(new PositionTextureVertex[]{v3,v4,v8,v7}); //up
        quads[2] = new TexturedQuad(new PositionTextureVertex[]{v5,v6,v7,v8}); //south
        quads[3] = new TexturedQuad(new PositionTextureVertex[]{v2,v1,v4,v3}); //north
        quads[4] = new TexturedQuad(new PositionTextureVertex[]{v6,v2,v3,v7}); //east
        quads[5] = new TexturedQuad(new PositionTextureVertex[]{v1,v5,v8,v4}); //west
    }

    public void render(Tessellator tess) {
        int id=0;
        for(FaceDirection dir : FaceDirection.values()) {
            if(!hidden.contains(dir)) {
                quads[id].draw(tess.getWorldRenderer(), 1/16f);
            }
            id++;
        }
    }
}
