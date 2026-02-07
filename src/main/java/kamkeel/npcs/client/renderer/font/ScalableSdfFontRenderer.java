package kamkeel.npcs.client.renderer.font;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScalableSdfFontRenderer {
    public static final ScalableSdfFontRenderer INSTANCE = new ScalableSdfFontRenderer();
    public static final ResourceLocation FONT_PATH = new ResourceLocation("customnpcs", "OpenSans.ttf");

    private static final int ATLAS_WIDTH = 1024;
    private static final int ATLAS_HEIGHT = 1024;
    private static final int PADDING = 4;
    private static final int SDF_SPREAD = 10;
    private static final int BASE_SIZE = 64;

    private final Map<Character, Glyph> sdfGlyphs = new HashMap<Character, Glyph>();
    private final Map<Integer, RasterAtlas> rasterAtlases = new HashMap<Integer, RasterAtlas>();
    private int sdfTextureId = -1;
    private int shaderProgram = -1;
    private boolean shaderReady;
    private boolean initialized;

    private Font baseFont;
    private final FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    public void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            loadFont();
            bakeSdfAtlas();
            shaderReady = compileShader();
        } catch (Exception e) {
            shaderReady = false;
            e.printStackTrace();
        }
    }

    private void loadFont() throws Exception {
        try (InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(FONT_PATH).getInputStream()) {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, input);
        }
    }

    private void bakeSdfAtlas() {
        Font font = baseFont.deriveFont(Font.PLAIN, BASE_SIZE);
        BufferedImage atlas = new BufferedImage(ATLAS_WIDTH, ATLAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, ATLAS_WIDTH, ATLAS_HEIGHT);
        g.dispose();

        int x = PADDING;
        int y = PADDING;
        int rowHeight = 0;

        for (char c = 32; c < 127; c++) {
            BufferedImage glyphImage = rasterGlyph(font, c);
            int w = glyphImage.getWidth();
            int h = glyphImage.getHeight();
            if (x + w + PADDING > ATLAS_WIDTH) {
                x = PADDING;
                y += rowHeight + PADDING;
                rowHeight = 0;
            }
            if (y + h + PADDING > ATLAS_HEIGHT) {
                throw new IllegalStateException("SDF atlas overflow");
            }

            BufferedImage sdfImage = toSdf(glyphImage);
            Graphics2D ag = atlas.createGraphics();
            ag.drawImage(sdfImage, x, y, null);
            ag.dispose();

            Rectangle2D bounds = font.getStringBounds(String.valueOf(c), fontRenderContext);
            float advance = (float) bounds.getWidth();

            Glyph glyph = new Glyph();
            glyph.width = w;
            glyph.height = h;
            glyph.advance = advance;
            glyph.u1 = (float) x / ATLAS_WIDTH;
            glyph.v1 = (float) y / ATLAS_HEIGHT;
            glyph.u2 = (float) (x + w) / ATLAS_WIDTH;
            glyph.v2 = (float) (y + h) / ATLAS_HEIGHT;
            glyph.offsetX = SDF_SPREAD;
            glyph.offsetY = SDF_SPREAD;
            sdfGlyphs.put(c, glyph);

            x += w + PADDING;
            rowHeight = Math.max(rowHeight, h);
        }

        int[] data = new int[ATLAS_WIDTH * ATLAS_HEIGHT];
        atlas.getRGB(0, 0, ATLAS_WIDTH, ATLAS_HEIGHT, data, 0, ATLAS_WIDTH);
        ByteBuffer pixels = ByteBuffer.allocateDirect(ATLAS_WIDTH * ATLAS_HEIGHT * 4);
        for (int color : data) {
            pixels.put((byte) ((color >> 16) & 0xFF));
            pixels.put((byte) ((color >> 8) & 0xFF));
            pixels.put((byte) (color & 0xFF));
            pixels.put((byte) ((color >> 24) & 0xFF));
        }
        pixels.flip();

        sdfTextureId = TextureUtil.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sdfTextureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ATLAS_WIDTH, ATLAS_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    }

    private BufferedImage rasterGlyph(Font font, char c) {
        BufferedImage img = new BufferedImage(BASE_SIZE * 2, BASE_SIZE * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 255));
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        g.drawString(String.valueOf(c), SDF_SPREAD, SDF_SPREAD + ascent);
        g.dispose();

        int minX = img.getWidth(), minY = img.getHeight(), maxX = 0, maxY = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int a = (img.getRGB(x, y) >>> 24) & 0xFF;
                if (a > 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new BufferedImage(SDF_SPREAD * 2 + 2, SDF_SPREAD * 2 + 2, BufferedImage.TYPE_INT_ARGB);
        }

        int w = (maxX - minX + 1) + SDF_SPREAD * 2;
        int h = (maxY - minY + 1) + SDF_SPREAD * 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D og = out.createGraphics();
        og.drawImage(img, SDF_SPREAD - minX, SDF_SPREAD - minY, null);
        og.dispose();
        return out;
    }

    private BufferedImage toSdf(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage sdf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean inside = ((source.getRGB(x, y) >>> 24) & 0xFF) > 127;
                float best = SDF_SPREAD;
                int rs = SDF_SPREAD;
                for (int oy = -rs; oy <= rs; oy++) {
                    int py = y + oy;
                    if (py < 0 || py >= h) continue;
                    for (int ox = -rs; ox <= rs; ox++) {
                        int px = x + ox;
                        if (px < 0 || px >= w) continue;
                        boolean pInside = ((source.getRGB(px, py) >>> 24) & 0xFF) > 127;
                        if (inside != pInside) {
                            float dist = (float) Math.sqrt(ox * ox + oy * oy);
                            if (dist < best) {
                                best = dist;
                            }
                        }
                    }
                }

                float signed = inside ? best : -best;
                float normalized = 0.5f + (signed / (2f * SDF_SPREAD));
                normalized = Math.max(0f, Math.min(1f, normalized));
                int alpha = (int) (normalized * 255f);
                int rgba = (alpha << 24) | 0x00FFFFFF;
                sdf.setRGB(x, y, rgba);
            }
        }

        return sdf;
    }

    private boolean compileShader() {
        if (!GL11.glGetString(GL11.GL_VERSION).contains("2")) {
            return false;
        }

        String vertexSrc =
            "#version 120\n" +
            "varying vec2 vUV;\n" +
            "void main(){\n" +
            "  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
            "  gl_FrontColor = gl_Color;\n" +
            "  vUV = gl_MultiTexCoord0.xy;\n" +
            "}";

        String fragSrc =
            "#version 120\n" +
            "uniform sampler2D uTex;\n" +
            "uniform float uSmooth;\n" +
            "varying vec2 vUV;\n" +
            "void main(){\n" +
            "  float dist = texture2D(uTex, vUV).a;\n" +
            "  float alpha = smoothstep(0.5 - uSmooth, 0.5 + uSmooth, dist);\n" +
            "  gl_FragColor = vec4(gl_Color.rgb, gl_Color.a * alpha);\n" +
            "}";

        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vs, vertexSrc);
        GL20.glCompileShader(vs);
        if (GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            return false;
        }

        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fs, fragSrc);
        GL20.glCompileShader(fs);
        if (GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            return false;
        }

        shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vs);
        GL20.glAttachShader(shaderProgram, fs);
        GL20.glLinkProgram(shaderProgram);
        return GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) != GL11.GL_FALSE;
    }

    public int drawString(String text, float x, float y, int size, int color) {
        ensureInitialized();
        if (shaderReady && sdfTextureId > 0) {
            return drawSdf(text, x, y, size, color);
        }
        return drawRaster(text, x, y, size, color);
    }

    private int drawSdf(String text, float x, float y, int size, int color) {
        float scale = size / (float) BASE_SIZE;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sdfTextureId);
        GL20.glUseProgram(shaderProgram);
        int texLoc = GL20.glGetUniformLocation(shaderProgram, "uTex");
        int smoothLoc = GL20.glGetUniformLocation(shaderProgram, "uSmooth");
        GL20.glUniform1i(texLoc, 0);
        GL20.glUniform1f(smoothLoc, Math.max(0.03f, 0.25f / scale));

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA(r, g, b, a);

        float cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = sdfGlyphs.get(c);
            if (glyph == null) {
                cursorX += size * 0.4f;
                continue;
            }

            float gw = glyph.width * scale;
            float gh = glyph.height * scale;
            float gx = cursorX - glyph.offsetX * scale;
            float gy = y - glyph.offsetY * scale;

            t.addVertexWithUV(gx, gy + gh, 0, glyph.u1, glyph.v2);
            t.addVertexWithUV(gx + gw, gy + gh, 0, glyph.u2, glyph.v2);
            t.addVertexWithUV(gx + gw, gy, 0, glyph.u2, glyph.v1);
            t.addVertexWithUV(gx, gy, 0, glyph.u1, glyph.v1);

            cursorX += glyph.advance * scale;
        }
        t.draw();

        GL20.glUseProgram(0);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        return (int) (cursorX - x);
    }

    private int drawRaster(String text, float x, float y, int size, int color) {
        RasterAtlas atlas = getRasterAtlas(size);
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.textureId);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA(r, g, b, a);

        float cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = atlas.glyphs.get(c);
            if (glyph == null) {
                cursorX += size * 0.4f;
                continue;
            }

            float gw = glyph.width;
            float gh = glyph.height;
            float gx = cursorX;
            float gy = y;

            t.addVertexWithUV(gx, gy + gh, 0, glyph.u1, glyph.v2);
            t.addVertexWithUV(gx + gw, gy + gh, 0, glyph.u2, glyph.v2);
            t.addVertexWithUV(gx + gw, gy, 0, glyph.u2, glyph.v1);
            t.addVertexWithUV(gx, gy, 0, glyph.u1, glyph.v1);
            cursorX += glyph.advance;
        }
        t.draw();

        GL11.glPopMatrix();
        GL11.glPopAttrib();
        return (int) (cursorX - x);
    }

    private RasterAtlas getRasterAtlas(int size) {
        RasterAtlas atlas = rasterAtlases.get(size);
        if (atlas != null) {
            return atlas;
        }

        Font font = baseFont.deriveFont(Font.PLAIN, size);
        BufferedImage atlasImage = new BufferedImage(ATLAS_WIDTH, ATLAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlasImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int x = PADDING;
        int y = PADDING;
        int rowHeight = 0;

        RasterAtlas rasterAtlas = new RasterAtlas();
        for (char c = 32; c < 127; c++) {
            String s = String.valueOf(c);
            int w = Math.max(1, fm.stringWidth(s));
            int h = Math.max(1, fm.getHeight());
            if (x + w + PADDING > ATLAS_WIDTH) {
                x = PADDING;
                y += rowHeight + PADDING;
                rowHeight = 0;
            }

            g.setColor(Color.WHITE);
            g.drawString(s, x, y + fm.getAscent());

            Glyph glyph = new Glyph();
            glyph.width = w;
            glyph.height = h;
            glyph.advance = w;
            glyph.u1 = (float) x / ATLAS_WIDTH;
            glyph.v1 = (float) y / ATLAS_HEIGHT;
            glyph.u2 = (float) (x + w) / ATLAS_WIDTH;
            glyph.v2 = (float) (y + h) / ATLAS_HEIGHT;
            rasterAtlas.glyphs.put(c, glyph);

            x += w + PADDING;
            rowHeight = Math.max(rowHeight, h);
        }
        g.dispose();

        int[] data = new int[ATLAS_WIDTH * ATLAS_HEIGHT];
        atlasImage.getRGB(0, 0, ATLAS_WIDTH, ATLAS_HEIGHT, data, 0, ATLAS_WIDTH);
        ByteBuffer pixels = ByteBuffer.allocateDirect(ATLAS_WIDTH * ATLAS_HEIGHT * 4);
        for (int color : data) {
            pixels.put((byte) ((color >> 16) & 0xFF));
            pixels.put((byte) ((color >> 8) & 0xFF));
            pixels.put((byte) (color & 0xFF));
            pixels.put((byte) ((color >> 24) & 0xFF));
        }
        pixels.flip();

        rasterAtlas.textureId = TextureUtil.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, rasterAtlas.textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ATLAS_WIDTH, ATLAS_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        rasterAtlases.put(size, rasterAtlas);
        return rasterAtlas;
    }

    public String getRendererPath() {
        ensureInitialized();
        return shaderReady ? "SDF shader (" + FONT_PATH + ")" : "Raster fallback (" + FONT_PATH + ")";
    }

    public String getAtlasDimensions() {
        return ATLAS_WIDTH + "x" + ATLAS_HEIGHT;
    }

    public int getLineHeight(int size) {
        return Math.max(1, size + 2);
    }

    private static class Glyph {
        float u1;
        float v1;
        float u2;
        float v2;
        float width;
        float height;
        float advance;
        float offsetX;
        float offsetY;
    }

    private static class RasterAtlas {
        final Map<Character, Glyph> glyphs = new HashMap<Character, Glyph>();
        int textureId;
    }
}
