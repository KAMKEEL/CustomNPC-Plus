package noppes.npcs.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.config.ConfigClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

@SideOnly(Side.CLIENT)
public class ImageDownloadAlt extends SimpleTexture {
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;

    public ImageDownloadAlt(File file, String url, ResourceLocation resource, IImageBuffer buffer) {
        super(resource);
        this.cacheFile = file;
        this.imageUrl = url;
        this.imageBuffer = buffer;
    }

    private void checkTextureUploaded() {
        if (!this.textureUploaded) {
            if (this.bufferedImage != null) {
                if (this.textureLocation != null) {
                    this.deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
                this.textureUploaded = true;
            }
        }
    }

    public int getGlTextureId() {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage p_147641_1_) {
        this.bufferedImage = p_147641_1_;

        if (this.imageBuffer != null) {
            this.imageBuffer.func_152634_a();
        }
    }

    public BufferedImage getBufferedImage() {
        return this.bufferedImage;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (this.bufferedImage == null && this.textureLocation != null) {
            super.loadTexture(resourceManager);
        }

        if (this.imageThread == null) {
            if (this.cacheFile != null && this.cacheFile.isFile()) {
                logger.debug("Loading http texture from local cache ({})", new Object[]{this.cacheFile});

                try {
                    this.bufferedImage = ImageIO.read(this.cacheFile);

                    if (this.imageBuffer != null) {
                        this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
                    }
                } catch (IOException ioexception) {
                    logger.error("Couldn\'t load skin " + this.cacheFile, ioexception);
                    this.loadTextureFromServer();
                }
            } else {
                this.loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer() {
        this.imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(ImageDownloadAlt.this.imageUrl);

                    // Adding a config so people can switch back if needed.
                    // TODO: Remove config option if improved works better
                    connection = ConfigClient.ImprovedImageDownloadConnection ? setupConnectionImproved(url) : setupConnectionOld(url);

                    connection.connect();

                    if (connection.getResponseCode() / 100 != 2) {
                        return;
                    }

                    BufferedImage bufferedimage;

                    if (ImageDownloadAlt.this.cacheFile != null) {
                        try (InputStream in = connection.getInputStream()) {
                            FileUtils.copyInputStreamToFile(in, ImageDownloadAlt.this.cacheFile);
                        }
                        bufferedimage = ImageIO.read(ImageDownloadAlt.this.cacheFile);
                    } else {
                        try (InputStream in = connection.getInputStream()) {
                            bufferedimage = ImageIO.read(in);
                        }
                    }

                    if (ImageDownloadAlt.this.imageBuffer != null) {
                        bufferedimage = ImageDownloadAlt.this.imageBuffer.parseUserSkin(bufferedimage);
                    }

                    ImageDownloadAlt.this.setBufferedImage(bufferedimage);
                } catch (MalformedURLException ignored) {
                } catch (Exception exception) {
                    ImageDownloadAlt.logger.error("Couldn\'t download http texture", exception);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }

    private HttpURLConnection setupConnectionOld(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (url).openConnection(Minecraft.getMinecraft().getProxy());
        // setups stuff which basically tells the browser if this a get or post request
        connection.setDoInput(true);
        connection.setDoOutput(false);

        // ??? I believe these are used to tell the receiver what you are sending to them
        // Not what you expect to receive from them.
        connection.setRequestProperty("Content-Type", "image/png");
        connection.setRequestProperty("Expect", "100-continue");
        //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Modify Accept Redirect
        if (isImgurLink(url)) {
            connection.setRequestProperty("Accept", "*/*");
        }
        return connection;
    }

    private HttpURLConnection setupConnectionImproved(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (url).openConnection(Minecraft.getMinecraft().getProxy());

        connection.setRequestMethod("GET");
        // Basically redundant because I set it as a GET request?
        // Better safe than sorry ig.
        connection.setDoInput(true);
        connection.setDoOutput(false);

        // Random ass user agent I grabbed from my snooping on imgur
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0");
        // I tell the server what I want to receive.
        // TODO: Should this just accept PNGs?
        connection.setRequestProperty("Accept", "image/png,image/*");

        return connection;
    }


    // Check if the URL is an Imgur link
    private static boolean isImgurLink(URL url) {
        return url.getHost().endsWith("imgur.com");
    }
}
