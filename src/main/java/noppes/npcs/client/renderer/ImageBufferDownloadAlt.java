package noppes.npcs.client.renderer;

import net.minecraft.client.renderer.ImageBufferDownload;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageBufferDownloadAlt extends ImageBufferDownload {
    private int imageData[];
    private int imageWidth;
    private int imageHeight;
    private boolean version;

    // If Version == true, use 64 Loader
    public ImageBufferDownloadAlt(boolean ver){
        this.version = ver;
    }

    @Override
    public BufferedImage parseUserSkin(BufferedImage bufferedimage) {

        if(this.version){
            imageWidth = bufferedimage.getWidth(null);
            imageHeight = bufferedimage.getHeight(null);

            BufferedImage bufferedimage1 = new BufferedImage(imageWidth, imageHeight, 2);
            Graphics g = bufferedimage1.getGraphics();
            g.drawImage(bufferedimage, 0, 0, null);
            g.dispose();

            imageData = ((DataBufferInt) bufferedimage1.getRaster().getDataBuffer()).getData();
            setAreaTransparent(imageWidth / 2, 0, imageWidth, imageHeight / 4);
            return bufferedimage1;
        }

        else{
            imageWidth = bufferedimage.getWidth(null);
            imageHeight = imageWidth / 2;

            BufferedImage bufferedimage1 = new BufferedImage(imageWidth, imageHeight, 2);
            Graphics g = bufferedimage1.getGraphics();
            g.drawImage(bufferedimage, 0, 0, null);
            g.dispose();
            imageData = ((DataBufferInt)bufferedimage1.getRaster().getDataBuffer()).getData();

            setAreaTransparent(imageWidth / 2, 0, imageWidth, imageHeight / 2);
            return bufferedimage1;
        }
    }

    /**
     * Makes the given area of the image transparent if it was previously completely opaque (used to remove the outer
     * layer of a skin around the head if it was saved all opaque; this would be redundant so it's assumed that the skin
     * maker is just using an image editor without an alpha channel)
     */
    private void setAreaTransparent(int par1, int par2, int par3, int par4) {
        if (!this.hasTransparency(par1, par2, par3, par4)) {
            for (int i1 = par1; i1 < par3; ++i1) {
                for (int j1 = par2; j1 < par4; ++j1) {
                    this.imageData[i1 + j1 * this.imageWidth] &= 16777215;
                }
            }
        }
    }

    /**
     * Returns true if the given area of the image contains transparent pixels
     */
    private boolean hasTransparency(int par1, int par2, int par3, int par4) {
        for (int i1 = par1; i1 < par3; ++i1) {
            for (int j1 = par2; j1 < par4; ++j1) {
                int k1 = this.imageData[i1 + j1 * this.imageWidth];

                if ((k1 >> 24 & 255) < 128) {
                    return true;
                }
            }
        }

        return false;
    }
}