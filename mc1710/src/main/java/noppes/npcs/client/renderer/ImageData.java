package noppes.npcs.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImageData {
    private final ResourceLocation location;
    private final boolean isUrl;

    private ImageDownloadAlt imageDownloadAlt = null;
    private BufferedImage bufferedImage = null;

    private int totalWidth, totalHeight;
    private boolean gotWidthHeight;
    private boolean invalid;

    // Animation fields
    private boolean animated = false;
    private int frameCount = 1;
    private int frametime = 2;
    private int[] frames = null;
    private boolean interpolate = false;
    private boolean mcmetaChecked = false;

    public ImageData(String directory) {
        this.location = new ResourceLocation(directory);
        if (directory.startsWith("https://") || directory.startsWith("http://")) {
            this.isUrl = true;
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            this.imageDownloadAlt = new ImageDownloadAlt(null, directory, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true, false));
            texturemanager.loadTexture(this.location, this.imageDownloadAlt);
        } else {
            this.isUrl = false;
        }
    }

    // For NPC Skins Only
    public ImageData(String directory, boolean x64, ResourceLocation resource) {
        this.location = resource;
        this.isUrl = true;
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        this.imageDownloadAlt = new ImageDownloadAlt(null, directory, SkinManager.field_152793_a, new ImageBufferDownloadAlt(x64));
        texturemanager.loadTexture(this.location, this.imageDownloadAlt);
    }

    public boolean imageLoaded() {
        if (!this.gotWidthHeight) {
            try {
                if (!this.isUrl) {
                    this.getWidthHeight();
                } else {
                    this.getURLWidthHeight();
                }
            } catch (Exception ignored) {
            }
        }
        return !this.invalid && this.location != null && this.gotWidthHeight;
    }

    public boolean invalid() {
        return this.invalid;
    }

    public void bindTexture() {
        ResourceLocation location = this.getLocation();
        if (location != null && !this.invalid) {
            try {
                Minecraft.getMinecraft().getTextureManager().bindTexture(location);
            } catch (Exception exception) {
                this.invalid = true;
            }
        }
    }

    public void renderEngineBind() {
        ResourceLocation location = this.getLocation();
        if (location != null && !this.invalid) {
            try {
                RenderNPCInterface.staticRenderManager.renderEngine.bindTexture(location);
            } catch (Exception exception) {
                this.invalid = true;
            }
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
                    try (InputStream inputstream = iresource.getInputStream()) {
                        this.bufferedImage = ImageIO.read(inputstream);
                    }
                } catch (IOException ignored) {
                }
            } else {
                this.bufferedImage = this.imageDownloadAlt.getBufferedImage();
            }
        }
        return this.bufferedImage;
    }

    private void getWidthHeight() throws IOException {
        if (this.invalid) {
            return;
        }

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
            this.invalid = true;
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
    }

    private void getURLWidthHeight() {
        if (this.imageDownloadAlt.getBufferedImage() != null && !this.invalid) {
            this.gotWidthHeight = true;
            this.totalWidth = this.imageDownloadAlt.getBufferedImage().getWidth();
            this.totalHeight = this.imageDownloadAlt.getBufferedImage().getHeight();
            correctWidthHeight();
        }
    }

    private void correctWidthHeight() {
        this.totalWidth = Math.max(this.totalWidth, 1);
        this.totalHeight = Math.max(this.totalHeight, 1);

        if (!this.isUrl && !this.mcmetaChecked) {
            this.mcmetaChecked = true;
            tryLoadMcmeta();
        }
    }

    public int getTotalWidth() {
        return this.gotWidthHeight ? this.totalWidth : -1;
    }

    public int getTotalHeight() {
        return this.gotWidthHeight ? this.totalHeight : -1;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════

    private void tryLoadMcmeta() {
        try {
            ResourceLocation mcmetaLoc = new ResourceLocation(
                location.getResourceDomain(),
                location.getResourcePath() + ".mcmeta"
            );
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(mcmetaLoc);
            InputStream stream = resource.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(stream);
                JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
                if (root.has("animation")) {
                    JsonObject anim = root.getAsJsonObject("animation");
                    this.animated = true;

                    if (anim.has("frametime")) {
                        this.frametime = Math.max(1, anim.get("frametime").getAsInt());
                    }
                    if (anim.has("interpolate")) {
                        this.interpolate = anim.get("interpolate").getAsBoolean();
                    }
                    if (anim.has("frames")) {
                        JsonArray framesArray = anim.getAsJsonArray("frames");
                        this.frames = new int[framesArray.size()];
                        int maxFrame = 0;
                        for (int i = 0; i < framesArray.size(); i++) {
                            this.frames[i] = framesArray.get(i).getAsInt();
                            if (this.frames[i] > maxFrame) {
                                maxFrame = this.frames[i];
                            }
                        }
                        this.frameCount = maxFrame + 1;
                    } else if (this.totalWidth > 0) {
                        this.frameCount = Math.max(1, this.totalHeight / this.totalWidth);
                    }
                }
            } finally {
                stream.close();
            }
        } catch (Exception ignored) {
            // No .mcmeta found or parse error — not animated
        }
    }

    /**
     * Programmatically enable animation on this ImageData.
     * Used when animation settings come from data class fields rather than .mcmeta files.
     */
    public void setAnimation(int frameCount, int frametime) {
        if (frameCount > 1) {
            this.animated = true;
            this.frameCount = Math.max(1, frameCount);
            this.frametime = Math.max(1, frametime);
        }
    }

    public boolean isAnimated() {
        return this.animated && this.frameCount > 1;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public int getFrameTime() {
        return this.frametime;
    }

    /**
     * Returns the pixel height of a single animation frame.
     * For non-animated textures, returns the total height.
     */
    public int getFrameHeight() {
        if (this.animated && this.frameCount > 1) {
            return Math.max(1, this.totalHeight / this.frameCount);
        }
        return this.gotWidthHeight ? this.totalHeight : -1;
    }

    /**
     * Returns the V-coordinate offset (0.0-1.0) for the current animation frame.
     * Based on system time so all instances of the same texture animate in sync.
     * Returns 0.0 for non-animated textures.
     */
    public float getCurrentFrameVOffset() {
        if (!this.animated || this.frameCount <= 1 || this.totalHeight <= 0) {
            return 0f;
        }

        long millis = Minecraft.getSystemTime();
        int tick = (int) (millis / 50L);
        int frameIdx;

        if (this.frames != null && this.frames.length > 0) {
            frameIdx = (tick / this.frametime) % this.frames.length;
            frameIdx = this.frames[frameIdx];
        } else {
            frameIdx = (tick / this.frametime) % this.frameCount;
        }

        int frameHeight = this.totalHeight / this.frameCount;
        return (float) (frameIdx * frameHeight) / (float) this.totalHeight;
    }
}
