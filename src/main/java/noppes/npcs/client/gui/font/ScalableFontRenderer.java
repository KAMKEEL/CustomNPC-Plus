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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

@SideOnly(Side.CLIENT)
public class ScalableFontRenderer {
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;
    private static final int[] DEFAULT_TIERS = new int[]{16, 24, 32, 48, 64, 96};
    private static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;

    private final ResourceLocation fontResource;
    private final String sourcePath;
    private final Map<Integer, BakedFontAtlas> tierToAtlas = new TreeMap<Integer, BakedFontAtlas>();
    private final boolean mipmapsEnabled;

    public static ScalableFontRenderer create(String sourcePath) {
        return new ScalableFontRenderer(sourcePath, new ResourceLocation("customnpcs", "OpenSans.ttf"));
    }

    private ScalableFontRenderer(String sourcePath, ResourceLocation fontResource) {
        this.sourcePath = sourcePath;
        this.fontResource = fontResource;
        this.mipmapsEnabled = false;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getRendererPath() {
        return "Tiered high-res atlas (downscale only)";
    }

    public boolean isMipmapsEnabled() {
        return mipmapsEnabled;
    }

    public int getTierForSize(int requestedSize) {
        int safe = Math.max(1, requestedSize);
        for (int tier : DEFAULT_TIERS) {
            if (tier >= safe) {
                return tier;
            }
        }
        return roundUpToMultiple(safe, 16);
    }

    public float getScaleForSize(int requestedSize) {
        int tier = getTierForSize(requestedSize);
        return requestedSize / (float) tier;
    }

    public String getCachedAtlasSummary() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, BakedFontAtlas> entry : tierToAtlas.entrySet()) {
            if (sb.length() > 0) {
                sb.append("  ");
            }
            BakedFontAtlas atlas = entry.getValue();
            sb.append(entry.getKey()).append("px=").append(atlas.width).append("x").append(atlas.height);
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    public String getAtlasSummaryForSize(int size) {
        int tier = getTierForSize(size);
        BakedFontAtlas atlas = getAtlasForTier(tier);
        return size + "px->" + tier + "px @" + atlas.width + "x" + atlas.height;
    }

    public String getDebugLineForSize(int requestedSize) {
        int tier = getTierForSize(requestedSize);
        float scale = requestedSize / (float) tier;
        BakedFontAtlas atlas = getAtlasForTier(tier);
        return "Req " + requestedSize + "px -> tier " + tier + "px, scale " + String.format("%.3f", scale) +
                ", atlas " + atlas.width + "x" + atlas.height + ", mipmaps " + (mipmapsEnabled ? "on" : "off");
    }

    public int getLineHeight(int size) {
        int tier = getTierForSize(size);
        float scale = size / (float) tier;
        return Math.max(1, Math.round(getAtlasForTier(tier).lineHeight * scale));
    }

    public int getStringWidth(String text, int size) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int tier = getTierForSize(size);
        float scale = size / (float) tier;
        BakedFontAtlas atlas = getAtlasForTier(tier);
        int tierWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) {
                tierWidth += tier / 2;
                continue;
            }
            tierWidth += atlas.glyphs[c - FIRST_CHAR].advance;
        }
        return Math.max(1, Math.round(tierWidth * scale));
    }

    public int drawString(String text, float x, float baselineY, int size, int color) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int tier = getTierForSize(size);
        float scale = size / (float) tier;
        BakedFontAtlas atlas = getAtlasForTier(tier);

        float drawX = Math.round(x);
        float drawBaseline = Math.round(baselineY);
        float cursorTier = 0f;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
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
                cursorTier += tier / 2f;
                continue;
            }

            Glyph glyph = atlas.glyphs[c - FIRST_CHAR];
            float x0 = Math.round((cursorTier + glyph.xOffset) * scale + drawX);
            float y0 = Math.round((glyph.yOffset) * scale + drawBaseline);
            float x1 = Math.round((cursorTier + glyph.xOffset + glyph.width) * scale + drawX);
            float y1 = Math.round((glyph.yOffset + glyph.height) * scale + drawBaseline);

            t.addVertexWithUV(x0, y1, 0, glyph.u0, glyph.v1);
            t.addVertexWithUV(x1, y1, 0, glyph.u1, glyph.v1);
            t.addVertexWithUV(x1, y0, 0, glyph.u1, glyph.v0);
            t.addVertexWithUV(x0, y0, 0, glyph.u0, glyph.v0);

            cursorTier += glyph.advance;
        }

        t.draw();

        GL11.glPopMatrix();
        GL11.glPopAttrib();

        return Math.max(1, Math.round(cursorTier * scale));
    }

    private BakedFontAtlas getAtlasForTier(int tier) {
        BakedFontAtlas cached = tierToAtlas.get(tier);
        if (cached != null) {
            return cached;
        }

        BakedFontAtlas baked = bakeAtlas(tier);
        tierToAtlas.put(tier, baked);
        return baked;
    }

    private BakedFontAtlas bakeAtlas(int tierSize) {
        Font font = loadFont(tierSize);

        BufferedImage probeImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probe = probeImage.createGraphics();
        probe.setFont(font);
        probe.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        probe.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        FontMetrics fm = probe.getFontMetrics();
        FontRenderContext frc = probe.getFontRenderContext();

        int lineHeight = fm.getHeight();
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int pad = 10;
        int atlasWidth = 1024;
        int x = 0;
        int y = 0;
        int currentRowMaxHeight = 0;

        Glyph[] glyphs = new Glyph[LAST_CHAR - FIRST_CHAR + 1];
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            char ch = (char) c;
            GlyphVector gv = font.createGlyphVector(frc, new char[]{ch});
            Rectangle bounds = gv.getPixelBounds(frc, 0, 0);

            int glyphWidth = Math.max(1, bounds.width);
            int glyphHeight = Math.max(1, bounds.height);
            int xOffset = bounds.x;
            int yOffset = bounds.y;
            int advance = Math.max(1, Math.round(gv.getGlyphMetrics(0).getAdvance()));

            int cellWidth = glyphWidth + pad * 2;
            if (x + cellWidth >= atlasWidth) {
                x = 0;
                y += currentRowMaxHeight + pad * 2;
                currentRowMaxHeight = 0;
            }

            glyphs[c - FIRST_CHAR] = new Glyph(ch, x + pad, y + pad, glyphWidth, glyphHeight, xOffset, yOffset, advance);
            x += cellWidth;
            if (glyphHeight > currentRowMaxHeight) {
                currentRowMaxHeight = glyphHeight;
            }
        }
        probe.dispose();

        int atlasHeight = y + currentRowMaxHeight + pad * 2;
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        for (Glyph glyph : glyphs) {
            GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), new char[]{glyph.character});
            g.drawGlyphVector(gv, glyph.x - glyph.xOffset, glyph.y - glyph.yOffset);
        }
        g.dispose();

        BufferedImage alphaAtlas = alphaOnlyFromCoverage(atlas);
        float invW = 1f / alphaAtlas.getWidth();
        float invH = 1f / alphaAtlas.getHeight();
        for (Glyph glyph : glyphs) {
            glyph.u0 = (glyph.x + 0.5f) * invW;
            glyph.v0 = (glyph.y + 0.5f) * invH;
            glyph.u1 = (glyph.x + glyph.width - 0.5f) * invW;
            glyph.v1 = (glyph.y + glyph.height - 0.5f) * invH;
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

    private BufferedImage alphaOnlyFromCoverage(BufferedImage source) {
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int py = 0; py < source.getHeight(); py++) {
            for (int px = 0; px < source.getWidth(); px++) {
                int argb = source.getRGB(px, py);
                int a = (argb >>> 24) & 255;
                int r = (argb >>> 16) & 255;
                int g = (argb >>> 8) & 255;
                int b = argb & 255;
                int coverage = Math.max(a, Math.max(r, Math.max(g, b)));
                out.setRGB(px, py, (coverage << 24) | 0x00FFFFFF);
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
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int argb = pixels[py * w + px];
                data.put((byte) ((argb >> 16) & 255));
                data.put((byte) ((argb >> 8) & 255));
                data.put((byte) (argb & 255));
                data.put((byte) ((argb >> 24) & 255));
            }
        }
        data.flip();

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmapsEnabled ? GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapsEnabled ? 1000 : 0);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        if (mipmapsEnabled) {
            GL30Compat.generateMipmap(GL11.GL_TEXTURE_2D);
        }
        return tex;
    }

    private int roundUpToMultiple(int value, int multiple) {
        return ((value + multiple - 1) / multiple) * multiple;
    }

    private static class Glyph {
        final char character;
        final int x;
        final int y;
        final int width;
        final int height;
        final int xOffset;
        final int yOffset;
        final int advance;
        float u0;
        float v0;
        float u1;
        float v1;

        private Glyph(char character, int x, int y, int width, int height, int xOffset, int yOffset, int advance) {
            this.character = character;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
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

    private static class GL30Compat {
        private static void generateMipmap(int target) {
            try {
                Class<?> gl30 = Class.forName("org.lwjgl.opengl.GL30");
                gl30.getMethod("glGenerateMipmap", int.class).invoke(null, target);
            } catch (Throwable ignored) {
                // Optional path only.
            }
        }
    }
}
