package noppes.npcs.client.gui.font;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScalableFontRenderer {
    private static final String VERT = "#version 120\n" +
            "varying vec2 vUv;\n" +
            "void main(){\n" +
            "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
            "vUv = gl_MultiTexCoord0.xy;\n" +
            "}";

    private static final String FRAG = "#version 120\n" +
            "uniform sampler2D tex;\n" +
            "uniform float edge;\n" +
            "uniform vec4 color;\n" +
            "varying vec2 vUv;\n" +
            "void main(){\n" +
            "float dist = texture2D(tex, vUv).r;\n" +
            "float alpha = smoothstep(0.5-edge, 0.5+edge, dist);\n" +
            "gl_FragColor = vec4(color.rgb, color.a * alpha);\n" +
            "}";

    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;
    private static final int SPREAD = 8;

    private final ResourceLocation fontResource;
    private final String sourcePath;
    private final float basePixelSize;
    private final GlyphData[] sdfGlyphs;
    private final int sdfTex;
    private final int atlasW;
    private final int atlasH;
    private final int shaderProgram;
    private final int edgeLoc;
    private final int colorLoc;
    private final int fallbackAtlasW;
    private final int fallbackAtlasH;
    private final Map<Integer, Atlas> fallbackAtlases = new HashMap<Integer, Atlas>();

    public static ScalableFontRenderer create(String sourcePath) {
        return new ScalableFontRenderer(sourcePath, new ResourceLocation("customnpcs", "OpenSans.ttf"));
    }

    private ScalableFontRenderer(String sourcePath, ResourceLocation fontResource) {
        this.sourcePath = sourcePath;
        this.fontResource = fontResource;
        this.basePixelSize = 64f;

        Font font = loadFont(basePixelSize);
        Atlas sdf = bakeAtlas(font, true);
        this.sdfGlyphs = sdf.glyphs;
        this.sdfTex = uploadAtlas(sdf.image);
        this.atlasW = sdf.width;
        this.atlasH = sdf.height;

        int shader = compileProgram(VERT, FRAG);
        this.shaderProgram = shader;
        this.edgeLoc = shader == 0 ? -1 : GL20.glGetUniformLocation(shader, "edge");
        this.colorLoc = shader == 0 ? -1 : GL20.glGetUniformLocation(shader, "color");

        Atlas fallbackBase = bakeAtlas(font, false);
        this.fallbackAtlasW = fallbackBase.width;
        this.fallbackAtlasH = fallbackBase.height;
        this.fallbackAtlases.put((int) basePixelSize, fallbackBase.withTexture(uploadAtlas(fallbackBase.image)));
    }

    public String getSourcePath() { return sourcePath; }
    public String getRendererPath() { return shaderProgram != 0 ? "SDF atlas + GL20 shader" : "Fallback per-size raster atlas"; }
    public int getAtlasWidth() { return shaderProgram != 0 ? atlasW : fallbackAtlasW; }
    public int getAtlasHeight() { return shaderProgram != 0 ? atlasH : fallbackAtlasH; }

    public int getStringWidth(String text, int size) {
        GlyphData[] glyphs = glyphsForSize(size);
        float scale = shaderProgram != 0 ? (float) size / basePixelSize : 1f;
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) { width += size * 0.5f; continue; }
            width += glyphs[c - FIRST_CHAR].advance * scale;
        }
        return Math.round(width);
    }

    public int drawString(String text, float x, float baselineY, int size, int color) {
        if (text == null || text.isEmpty()) return 0;

        Atlas activeAtlas = null;
        GlyphData[] glyphs;
        float scale;
        if (shaderProgram != 0) {
            glyphs = sdfGlyphs;
            scale = (float) size / basePixelSize;
        } else {
            activeAtlas = getFallback(size);
            glyphs = activeAtlas.glyphs;
            scale = 1f;
        }

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int texture = shaderProgram != 0 ? sdfTex : activeAtlas.texture;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        float a = ((color >> 24) & 255) / 255f;

        if (shaderProgram != 0) {
            GL20.glUseProgram(shaderProgram);
            GL20.glUniform1f(edgeLoc, Math.max(0.03f, 0.2f / scale));
            GL20.glUniform4f(colorLoc, r, g, b, a <= 0 ? 1f : a);
        } else {
            GL11.glColor4f(r, g, b, a <= 0 ? 1f : a);
        }

        float cursorX = x;
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) { cursorX += size * 0.5f; continue; }
            GlyphData glyph = glyphs[c - FIRST_CHAR];
            float x0 = cursorX + glyph.bearingX * scale;
            float y0 = baselineY - glyph.bearingY * scale;
            float x1 = x0 + glyph.width * scale;
            float y1 = y0 + glyph.height * scale;
            t.addVertexWithUV(x0, y1, 0, glyph.u0, glyph.v1);
            t.addVertexWithUV(x1, y1, 0, glyph.u1, glyph.v1);
            t.addVertexWithUV(x1, y0, 0, glyph.u1, glyph.v0);
            t.addVertexWithUV(x0, y0, 0, glyph.u0, glyph.v0);
            cursorX += glyph.advance * scale;
        }
        t.draw();
        if (shaderProgram != 0) GL20.glUseProgram(0);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        return Math.round(cursorX - x);
    }

    private GlyphData[] glyphsForSize(int size) {
        return shaderProgram != 0 ? sdfGlyphs : getFallback(size).glyphs;
    }

    private Atlas getFallback(int size) {
        Atlas cached = fallbackAtlases.get(size);
        if (cached != null) return cached;
        Atlas atlas = bakeAtlas(loadFont(size), false);
        Atlas withTex = atlas.withTexture(uploadAtlas(atlas.image));
        fallbackAtlases.put(size, withTex);
        return withTex;
    }

    private Font loadFont(float size) {
        try {
            InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(fontResource).getInputStream();
            try {
                return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load font " + fontResource, e);
        }
    }

    private Atlas bakeAtlas(Font font, boolean sdf) {
        BufferedImage probe = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = probe.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = g.getFontMetrics();

        int pad = sdf ? SPREAD + 2 : 2;
        int rowH = metrics.getHeight() + pad * 2;
        int x = 0, y = 0, width = 1024;
        GlyphData[] glyphs = new GlyphData[LAST_CHAR - FIRST_CHAR + 1];
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            int gw = Math.max(1, metrics.charWidth((char) c));
            int cellW = gw + pad * 2;
            if (x + cellW >= width) { x = 0; y += rowH; }
            glyphs[c - FIRST_CHAR] = new GlyphData(x + pad, y + pad, gw, metrics.getHeight(), metrics.getAscent(), metrics.charWidth((char) c));
            x += cellW;
        }
        g.dispose();

        int height = y + rowH;
        BufferedImage atlas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = atlas.createGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = graphics.getFontMetrics();
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            GlyphData gd = glyphs[c - FIRST_CHAR];
            graphics.drawString(String.valueOf((char) c), gd.x, gd.y + fm.getAscent());
            gd.width = Math.max(1, fm.charWidth((char) c));
            gd.height = fm.getHeight();
            gd.bearingY = fm.getAscent();
            gd.advance = fm.charWidth((char) c);
        }
        graphics.dispose();

        BufferedImage finalAtlas = sdf ? makeSdf(atlas) : alphaOnly(atlas);
        for (GlyphData gd : glyphs) {
            int w = finalAtlas.getWidth(), h = finalAtlas.getHeight();
            gd.u0 = (float) gd.x / w;
            gd.v0 = (float) gd.y / h;
            gd.u1 = (float) (gd.x + gd.width) / w;
            gd.v1 = (float) (gd.y + gd.height) / h;
        }
        return new Atlas(finalAtlas, finalAtlas.getWidth(), finalAtlas.getHeight(), 0, glyphs);
    }

    private BufferedImage alphaOnly(BufferedImage source) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int a = (source.getRGB(x, y) >>> 24) & 255;
                out.setRGB(x, y, (a << 24) | 0x00FFFFFF);
            }
        }
        return out;
    }

    private BufferedImage makeSdf(BufferedImage source) {
        int w = source.getWidth(), h = source.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        boolean[][] inside = new boolean[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                inside[x][y] = ((source.getRGB(x, y) >>> 24) & 255) > 32;
            }
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float d = nearestDistance(inside, w, h, x, y, inside[x][y]);
                float signed = inside[x][y] ? d : -d;
                float normalized = Math.max(0f, Math.min(1f, 0.5f + (signed / (SPREAD * 2f))));
                int v = (int) (normalized * 255f);
                out.setRGB(x, y, (v << 24) | (v << 16) | (v << 8) | v);
            }
        }
        return out;
    }

    private float nearestDistance(boolean[][] inside, int w, int h, int px, int py, boolean currentInside) {
        float best = SPREAD;
        int minX = Math.max(0, px - SPREAD), maxX = Math.min(w - 1, px + SPREAD);
        int minY = Math.max(0, py - SPREAD), maxY = Math.min(h - 1, py + SPREAD);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (inside[x][y] != currentInside) {
                    float dx = x - px, dy = y - py;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < best) best = dist;
                }
            }
        }
        return best;
    }

    private int uploadAtlas(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);
        ByteBuffer data = BufferUtils.createByteBuffer(w * h * 4);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];
                data.put((byte) ((argb >> 16) & 255));
                data.put((byte) ((argb >> 8) & 255));
                data.put((byte) (argb & 255));
                data.put((byte) ((argb >> 24) & 255));
            }
        }
        data.flip();

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 0x812F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 0x812F);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        return tex;
    }

    private int compileProgram(String vsSrc, String fsSrc) {
        try {
            int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vs, vsSrc);
            GL20.glCompileShader(vs);
            if (GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS) == 0) return 0;
            int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fs, fsSrc);
            GL20.glCompileShader(fs);
            if (GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS) == 0) return 0;
            int program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vs);
            GL20.glAttachShader(program, fs);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) return 0;
            return program;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static class GlyphData {
        int x, y, width, height, bearingX, bearingY, advance;
        float u0, v0, u1, v1;
        GlyphData(int x, int y, int width, int height, int bearingY, int advance) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.bearingX = 0; this.bearingY = bearingY; this.advance = advance;
        }
    }

    private static class Atlas {
        final BufferedImage image;
        final int width, height, texture;
        final GlyphData[] glyphs;
        Atlas(BufferedImage image, int width, int height, int texture, GlyphData[] glyphs) {
            this.image = image; this.width = width; this.height = height; this.texture = texture; this.glyphs = glyphs;
        }
        Atlas withTexture(int texture) { return new Atlas(image, width, height, texture, glyphs); }
    }
}
