package noppes.npcs.client.model.skin3d;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransparentPixelWrapper {
    private static final int[][] OFFSETS = new int[][]{{0,1},{0,-1},{1,0},{-1,0}};
    private static final FaceDirection[] HIDDEN_N = new FaceDirection[]{FaceDirection.WEST,FaceDirection.EAST,FaceDirection.UP,FaceDirection.DOWN};
    private static final FaceDirection[] HIDDEN_S = new FaceDirection[]{FaceDirection.EAST,FaceDirection.WEST,FaceDirection.UP,FaceDirection.DOWN};
    private static final FaceDirection[] HIDDEN_W = new FaceDirection[]{FaceDirection.SOUTH,FaceDirection.NORTH,FaceDirection.UP,FaceDirection.DOWN};
    private static final FaceDirection[] HIDDEN_E = new FaceDirection[]{FaceDirection.NORTH,FaceDirection.SOUTH,FaceDirection.UP,FaceDirection.DOWN};
    private static final FaceDirection[] HIDDEN_UD = new FaceDirection[]{FaceDirection.EAST,FaceDirection.WEST,FaceDirection.NORTH,FaceDirection.SOUTH};

    public static PixelModelPart wrapBox(BufferedImage img, int width, int height, int depth, int texU, int texV) {
        List<PixelCube> cubes = new ArrayList<PixelCube>();
        float pixel = 1f;
        float ox = -width/2f;
        float oy = -height;
        float oz = -depth/2f;
        for(int u=0;u<width;u++) {
            for(int v=0;v<height;v++) {
                addPixel(img,cubes,pixel,isBorder(u,v,width,height),texU+depth+u,texV+depth+v,ox+u,oy+v,oz,FaceDirection.SOUTH);
                addPixel(img,cubes,pixel,isBorder(u,v,width,height),texU+2*depth+width+u,texV+depth+v,ox+width-1-u,oy+v,oz+depth-1,FaceDirection.NORTH);
            }
        }
        for(int u=0;u<depth;u++) {
            for(int v=0;v<height;v++) {
                addPixel(img,cubes,pixel,isBorder(u,v,depth,height),texU-1+depth-u,texV+depth+v,ox,oy+v,oz+u,FaceDirection.EAST);
                addPixel(img,cubes,pixel,isBorder(u,v,depth,height),texU+depth+width+u,texV+depth+v,ox+width-1f,oy+v,oz+u,FaceDirection.WEST);
            }
        }
        for(int u=0;u<width;u++) {
            for(int v=0;v<depth;v++) {
                addPixel(img,cubes,pixel,isBorder(u,v,width,depth),texU+depth+u,texV+depth-1-v,ox+u,oy,oz+v,FaceDirection.UP);
                addPixel(img,cubes,pixel,isBorder(u,v,width,depth),texU+depth+width+u,texV+depth-1-v,ox+u,oy+height-1f,oz+v,FaceDirection.DOWN);
            }
        }
        return new PixelModelPart(cubes);
    }

    private static boolean isBorder(int u,int v,int w,int h){
        return u==0 || v==0 || u==w-1 || v==h-1;
    }

    private static void addPixel(BufferedImage img,List<PixelCube> cubes,float pixel,boolean border,int u,int v,float x,float y,float z,FaceDirection dir){
        if(getAlpha(img,u,v)!=0){
            Set<FaceDirection> hide = new HashSet<FaceDirection>();
            if(!border){
                for(int i=0;i<OFFSETS.length;i++){
                    int tu=u+OFFSETS[i][1];
                    int tv=v+OFFSETS[i][0];
                    if(tu>=0 && tu<img.getWidth() && tv>=0 && tv<img.getHeight() && getAlpha(img,tu,tv)!=0){
                        if(dir==FaceDirection.NORTH)hide.add(HIDDEN_N[i]);
                        if(dir==FaceDirection.SOUTH)hide.add(HIDDEN_S[i]);
                        if(dir==FaceDirection.EAST)hide.add(HIDDEN_E[i]);
                        if(dir==FaceDirection.WEST)hide.add(HIDDEN_W[i]);
                        if(dir==FaceDirection.UP||dir==FaceDirection.DOWN)hide.add(HIDDEN_UD[i]);
                    }
                }
                hide.add(dir);
            }
            cubes.addAll(PixelCubeListBuilder.create()
                    .texSize(img.getWidth(), img.getHeight())
                    .texOffs(u-2,v-1)
                    .addBox(x,y,z,pixel,hide.toArray(new FaceDirection[hide.size()]))
                    .getCubes());
        }
    }

    private static int getAlpha(BufferedImage img,int x,int y){
        int argb = img.getRGB(x,y);
        return argb>>>24;
    }
}
