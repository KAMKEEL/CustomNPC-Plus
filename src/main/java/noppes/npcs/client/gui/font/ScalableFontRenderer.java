package noppes.npcs.client.gui.font;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

@SideOnly(Side.CLIENT)
public class ScalableFontRenderer {
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;

    private final ResourceLocation fontResource;
    private final String sourcePath;
    private final Map<Integer, BakedFontAtlas> sizeToAtlas = new TreeMap<Integer, BakedFontAtlas>();

    public static ScalableFontRenderer create(String sourcePath) {
        return new ScalableFontRenderer(sourcePath, new ResourceLocation("customnpcs", "OpenSans.ttf"));
    }

    private ScalableFontRenderer(String sourcePath, ResourceLocation fontResource) {
        this.sourcePath = sourcePath;
        this.fontResource = fontResource;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getRendererPath() {
        return "Per-size baked atlas (no scaling)";
    }

    public String getCachedAtlasSummary() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, BakedFontAtlas> entry : sizeToAtlas.entrySet()) {
            if (sb.length() > 0) {
                sb.append("  ");
            }
            BakedFontAtlas atlas = entry.getValue();
            sb.append(entry.getKey()).append("px=").append(atlas.width).append("x").append(atlas.height);
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    public String getAtlasSummaryForSize(int size) {
        BakedFontAtlas atlas = getAtlas(size);
        return size + "px=" + atlas.width + "x" + atlas.height;
    }

    public int getLineHeight(int size) {
        return getAtlas(size).lineHeight;
    }

    public int getStringWidth(String text, int size) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        BakedFontAtlas atlas = getAtlas(size);
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) {
                width += size / 2;
                continue;
            }
            width += atlas.glyphs[c - FIRST_CHAR].advance;
        }
        return width;
    }

    public int drawString(String text, float x, float baselineY, int size, int color) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        BakedFontAtlas atlas = getAtlas(size);
        float drawX = Math.round(x);
        float drawBaseline = Math.round(baselineY);
        float cursorX = drawX;
        float topY = drawBaseline - atlas.ascent;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.textureId);

        float a = ((color >> 24) & 255) / 255f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        GL11.glColor4f(r, g, b, a <= 0f ? 1f : a);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) {
                cursorX += size / 2f;
                continue;
            }

            Glyph glyph = atlas.glyphs[c - FIRST_CHAR];
            float x0 = Math.round(cursorX);
            float y0 = topY;
            float x1 = x0 + glyph.width;
            float y1 = y0 + atlas.lineHeight;

            t.addVertexWithUV(x0, y1, 0, glyph.u0, glyph.v1);
            t.addVertexWithUV(x1, y1, 0, glyph.u1, glyph.v1);
            t.addVertexWithUV(x1, y0, 0, glyph.u1, glyph.v0);
            t.addVertexWithUV(x0, y0, 0, glyph.u0, glyph.v0);

            cursorX += glyph.advance;
        }

        t.draw();

        GL11.glPopMatrix();
        GL11.glPopAttrib();

        return Math.round(cursorX - drawX);
    }

    private BakedFontAtlas getAtlas(int size) {
        BakedFontAtlas cached = sizeToAtlas.get(size);
        if (cached != null) {
            return cached;
        }

        BakedFontAtlas baked = bakeAtlas(size);
        sizeToAtlas.put(size, baked);
        return baked;
    }

    private BakedFontAtlas bakeAtlas(int size) {
        Font font = loadFont(size);

        BufferedImage probeImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probe = probeImage.createGraphics();
        probe.setFont(font);
        probe.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = probe.getFontMetrics();

        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int lineHeight = fm.getHeight();
        int pad = 2;
        int atlasWidth = 1024;
        int rowHeight = lineHeight + pad * 2;
        int x = 0;
        int y = 0;

        Glyph[] glyphs = new Glyph[LAST_CHAR - FIRST_CHAR + 1];
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            int glyphWidth = Math.max(1, fm.charWidth((char) c));
            int cellWidth = glyphWidth + pad * 2;
            if (x + cellWidth >= atlasWidth) {
                x = 0;
                y += rowHeight;
            }

            glyphs[c - FIRST_CHAR] = new Glyph(x + pad, y + pad, glyphWidth, Math.max(1, glyphWidth));
            x += cellWidth;
        }
        probe.dispose();

        int atlasHeight = y + rowHeight;
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            Glyph glyph = glyphs[c - FIRST_CHAR];
            g.drawString(String.valueOf((char) c), glyph.x, glyph.y + ascent);
        }
        g.dispose();

        BufferedImage alphaAtlas = alphaOnly(atlas);
        for (Glyph glyph : glyphs) {
            glyph.u0 = (float) glyph.x / alphaAtlas.getWidth();
            glyph.v0 = (float) glyph.y / alphaAtlas.getHeight();
            glyph.u1 = (float) (glyph.x + glyph.width) / alphaAtlas.getWidth();
            glyph.v1 = (float) (glyph.y + lineHeight) / alphaAtlas.getHeight();
        }

        int textureId = uploadAtlas(alphaAtlas);
        return new BakedFontAtlas(textureId, alphaAtlas.getWidth(), alphaAtlas.getHeight(), lineHeight, ascent, descent, glyphs);
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

    private int uploadAtlas(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
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
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        return tex;
    }

    private static class Glyph {
        final int x;
        final int y;
        final int width;
        final int advance;
        float u0;
        float v0;
        float u1;
        float v1;

        private Glyph(int x, int y, int width, int advance) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.advance = advance;
        }
    }

    public static class BakedFontAtlas {
        public final int textureId;
        public final int width;
        public final int height;
        public final int lineHeight;
        public final int ascent;
        public final int descent;
        private final Glyph[] glyphs;

        private BakedFontAtlas(int textureId, int width, int height, int lineHeight, int ascent, int descent, Glyph[] glyphs) {
            this.textureId = textureId;
            this.width = width;
            this.height = height;
            this.lineHeight = lineHeight;
            this.ascent = ascent;
            this.descent = descent;
            this.glyphs = glyphs;
        }
    }
}
