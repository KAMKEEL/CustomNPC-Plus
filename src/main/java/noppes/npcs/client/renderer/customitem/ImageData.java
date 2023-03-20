package noppes.npcs.client.renderer.customitem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageData {
    private ResourceLocation location;
    private final boolean isUrl;
    private ImageDownloadAlt imageDownloadAlt = null;
    private BufferedImage bufferedImage = null;
    private int totalWidth, totalHeight;
    private boolean gotWidthHeight;

    public ImageData(String directory) {
        this.location = new ResourceLocation(directory);
        if (directory.startsWith("https://")) {
            this.isUrl = true;
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            this.imageDownloadAlt = new ImageDownloadAlt(null, directory, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true, false));
            texturemanager.loadTexture(location, this.imageDownloadAlt);
        } else {
            this.isUrl = false;
        }
    }

    public ResourceLocation getLocation() {
        return this.isUrl && this.imageDownloadAlt.getBufferedImage() == null ? null : this.location;
    }

    public BufferedImage getBufferedImage() {
        if (this.bufferedImage == null) {
            if (!this.isUrl) {
                try {
                    IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
                    InputStream inputstream = iresource.getInputStream();
                    this.bufferedImage = ImageIO.read(inputstream);
                } catch (IOException ignored) {}
            } else {
                this.bufferedImage = this.imageDownloadAlt.getBufferedImage();
            }
        }
        return this.bufferedImage;
    }

    private void getWidthHeight() throws IOException {
        InputStream inputstream = null;

        try {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(this.location);
            inputstream = iresource.getInputStream();
            BufferedImage bufferedimage = ImageIO.read(inputstream);
            this.gotWidthHeight = true;
            this.totalWidth = bufferedimage.getWidth();
            this.totalHeight = bufferedimage.getHeight();
            correctWidthHeight();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
    }

    private void getURLWidthHeight(){
        if(this.imageDownloadAlt.getBufferedImage() != null) {
            this.gotWidthHeight = true;
            this.totalWidth = this.imageDownloadAlt.getBufferedImage().getWidth();
            this.totalHeight = this.imageDownloadAlt.getBufferedImage().getHeight();
            correctWidthHeight();
        }
    }

    private void correctWidthHeight(){
        this.totalWidth = Math.max(this.totalWidth, 1);
        this.totalHeight = Math.max(this.totalHeight, 1);
    }

    public int getTotalWidth() {
        if (!this.gotWidthHeight) {
            try {
                if (!this.isUrl) {
                    this.getWidthHeight();
                } else {
                    this.getURLWidthHeight();
                }
            } catch (Exception ignored) {}
            if (!this.gotWidthHeight) {
                return -1;
            }
        }
        return this.totalWidth;
    }

    public int getTotalHeight() {
        if (!this.gotWidthHeight) {
            try {
                if (!this.isUrl) {
                    this.getWidthHeight();
                } else {
                    this.getURLWidthHeight();
                }
            } catch (Exception ignored) {}
            if (!this.gotWidthHeight) {
                return -1;
            }
        }
        return this.totalHeight;
    }
}
