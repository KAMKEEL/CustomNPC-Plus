package kamkeel.npcs.client.renderer.font;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScalableFontRenderer {
    private static final int BASE_FONT_SIZE = 96;
    private static final int PADDING = 12;
    private static final int SDF_SPREAD = 10;
    private static final char FIRST_CHAR = 32;
    private static final char LAST_CHAR = 126;

    private static final String RENDER_PATH_SHADER = "sdf-shader";
    private static final String RENDER_PATH_FALLBACK = "rebake-per-size";

    private static ScalableFontRenderer instance;

    private final Font baseFont;
    private final Map<Character, GlyphInfo> baseGlyphs;
    private final Atlas baseAtlas;
    private final float baseAscent;
    private final float baseLineHeight;
    private final Map<Integer, AtlasCacheEntry> fallbackCache = new HashMap<Integer, AtlasCacheEntry>();

    private int shaderProgram;
    private int uniformTexture;
    private int uniformEdge;
    private int uniformSmoothing;
    private boolean shaderReady;

    private ScalableFontRenderer() {
        this.baseFont = loadFont();
        AtlasBuild baseBuild = buildAtlas(baseFont.deriveFont((float) BASE_FONT_SIZE), true);
        this.baseGlyphs = baseBuild.glyphs;
        this.baseAtlas = uploadAtlas(baseBuild.image);
        this.baseAscent = baseBuild.ascent;
        this.baseLineHeight = baseBuild.lineHeight;
        this.shaderReady = initShader();
    }

    public static ScalableFontRenderer get() {
        if (instance == null) {
            instance = new ScalableFontRenderer();
        }
        return instance;
    }

    public String getRendererPath() {
        return shaderReady ? RENDER_PATH_SHADER : RENDER_PATH_FALLBACK;
    }

    public int getAtlasWidth() {
        if (shaderReady) {
            return baseAtlas.width;
        }
        AtlasCacheEntry entry = getFallbackEntry(16);
        return entry.atlas.width;
    }

    public int getAtlasHeight() {
        if (shaderReady) {
            return baseAtlas.height;
        }
        AtlasCacheEntry entry = getFallbackEntry(16);
        return entry.atlas.height;
    }

    public float getLineHeight(float pxSize) {
        return shaderReady ? baseLineHeight * (pxSize / BASE_FONT_SIZE) : pxSize * 1.2f;
    }

    public void drawString(String text, float x, float y, float pxSize, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        float a = ((color >>> 24) & 0xFF) / 255f;
        float r = ((color >>> 16) & 0xFF) / 255f;
        float g = ((color >>> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (shaderReady) {
            drawShader(text, x, y, pxSize, r, g, b, a);
        } else {
            drawFallback(text, x, y, Math.round(pxSize), r, g, b, a);
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void drawShader(String text, float x, float y, float pxSize, float r, float g, float b, float a) {
        float scale = pxSize / BASE_FONT_SIZE;
        float baseline = y + baseAscent * scale;
        float cursor = x;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, baseAtlas.textureId);
        GL20.glUseProgram(shaderProgram);
        GL20.glUniform1i(uniformTexture, 0);
        GL20.glUniform1f(uniformEdge, 0.5f);
        GL20.glUniform1f(uniformSmoothing, Math.max(0.03f, 0.18f / Math.max(scale, 0.25f)));

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, a);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                cursor = x;
                baseline += baseLineHeight * scale;
                continue;
            }

            GlyphInfo glyph = baseGlyphs.get(c);
            if (glyph == null) {
                glyph = baseGlyphs.get('?');
            }
            if (glyph == null) {
                continue;
            }

            float x0 = cursor + glyph.offsetX * scale;
            float y0 = baseline + glyph.offsetY * scale;
            float x1 = x0 + glyph.width * scale;
            float y1 = y0 + glyph.height * scale;

            t.addVertexWithUV(x0, y1, 0, glyph.u0, glyph.v1);
            t.addVertexWithUV(x1, y1, 0, glyph.u1, glyph.v1);
            t.addVertexWithUV(x1, y0, 0, glyph.u1, glyph.v0);
            t.addVertexWithUV(x0, y0, 0, glyph.u0, glyph.v0);

            cursor += glyph.advance * scale;
        }

        t.draw();
        GL20.glUseProgram(0);
    }

    private void drawFallback(String text, float x, float y, int pxSize, float r, float g, float b, float a) {
        AtlasCacheEntry entry = getFallbackEntry(Math.max(1, pxSize));
        float baseline = y + entry.ascent;
        float cursor = x;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, entry.atlas.textureId);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, a);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                cursor = x;
                baseline += entry.lineHeight;
                continue;
            }

            GlyphInfo glyph = entry.glyphs.get(c);
            if (glyph == null) {
                glyph = entry.glyphs.get('?');
            }
            if (glyph == null) {
                continue;
            }

            float x0 = cursor + glyph.offsetX;
            float y0 = baseline + glyph.offsetY;
            float x1 = x0 + glyph.width;
            float y1 = y0 + glyph.height;

            t.addVertexWithUV(x0, y1, 0, glyph.u0, glyph.v1);
            t.addVertexWithUV(x1, y1, 0, glyph.u1, glyph.v1);
            t.addVertexWithUV(x1, y0, 0, glyph.u1, glyph.v0);
            t.addVertexWithUV(x0, y0, 0, glyph.u0, glyph.v0);

            cursor += glyph.advance;
        }

        t.draw();
    }

    private AtlasCacheEntry getFallbackEntry(int size) {
        AtlasCacheEntry cached = fallbackCache.get(size);
        if (cached != null) {
            return cached;
        }

        AtlasBuild build = buildAtlas(baseFont.deriveFont((float) size), false);
        Atlas atlas = uploadAtlas(build.image);
        AtlasCacheEntry entry = new AtlasCacheEntry(atlas, build.glyphs, build.ascent, build.lineHeight);
        fallbackCache.put(size, entry);
        return entry;
    }

    private Font loadFont() {
        try {
            InputStream stream = ScalableFontRenderer.class.getClassLoader().getResourceAsStream("assets/customnpcs/OpenSans.ttf");
            if (stream == null) {
                throw new IllegalStateException("TTF not found: assets/customnpcs/OpenSans.ttf");
            }
            Font loaded = Font.createFont(Font.TRUETYPE_FONT, stream);
            return loaded.deriveFont(Font.PLAIN, (float) BASE_FONT_SIZE);
        } catch (Exception e) {
            throw new RuntimeException("Failed loading OpenSans.ttf", e);
        }
    }

    private AtlasBuild buildAtlas(Font font, boolean sdf) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        float ascent = font.getLineMetrics("Ag", frc).getAscent();
        float lineHeight = font.getLineMetrics("Ag", frc).getHeight();

        int maxGlyphHeight = (int) Math.ceil(lineHeight) + (PADDING * 2);
        int atlasWidth = 1024;
        int x = PADDING;
        int y = PADDING;
        int rowHeight = maxGlyphHeight;

        Map<Character, GlyphPlacement> placements = new HashMap<Character, GlyphPlacement>();
        for (char c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            GlyphVector gv = font.createGlyphVector(frc, new char[]{c});
            Rectangle bounds = gv.getGlyphPixelBounds(0, frc, 0, 0);
            int glyphWidth = Math.max(1, bounds.width);
            int glyphHeight = Math.max(1, bounds.height);
            int advance = (int) Math.ceil(gv.getGlyphMetrics(0).getAdvanceX());

            int packedWidth = glyphWidth + (PADDING * 2);
            if (x + packedWidth >= atlasWidth) {
                x = PADDING;
                y += rowHeight;
            }
            placements.put(c, new GlyphPlacement(x + PADDING, y + PADDING, bounds, advance, gv.getGlyphMetrics(0)));
            x += packedWidth;
        }

        int atlasHeight = y + rowHeight + PADDING;
        BufferedImage alpha = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = alpha.createGraphics();
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);

        Map<Character, GlyphInfo> glyphs = new HashMap<Character, GlyphInfo>();
        for (Map.Entry<Character, GlyphPlacement> entry : placements.entrySet()) {
            char c = entry.getKey();
            GlyphPlacement p = entry.getValue();
            float baseline = p.y - p.bounds.y;
            g.drawString(String.valueOf(c), p.x, baseline);

            int width = Math.max(1, p.bounds.width);
            int height = Math.max(1, p.bounds.height);
            float u0 = (float) p.x / atlasWidth;
            float v0 = (float) p.y / atlasHeight;
            float u1 = (float) (p.x + width) / atlasWidth;
            float v1 = (float) (p.y + height) / atlasHeight;

            glyphs.put(c, new GlyphInfo(u0, v0, u1, v1, width, height, p.bounds.x, p.bounds.y, p.metrics.getAdvanceX()));
        }
        g.dispose();

        BufferedImage finalImage = sdf ? createSdfImage(alpha) : alpha;
        return new AtlasBuild(finalImage, glyphs, ascent, lineHeight);
    }

    private BufferedImage createSdfImage(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        boolean[] inside = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                inside[y * width + x] = a > 127;
            }
        }

        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int spreadSq = SDF_SPREAD * SDF_SPREAD;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isInside = inside[y * width + x];
                int bestSq = spreadSq;
                int minX = Math.max(0, x - SDF_SPREAD);
                int maxX = Math.min(width - 1, x + SDF_SPREAD);
                int minY = Math.max(0, y - SDF_SPREAD);
                int maxY = Math.min(height - 1, y + SDF_SPREAD);
                for (int yy = minY; yy <= maxY; yy++) {
                    for (int xx = minX; xx <= maxX; xx++) {
                        if (inside[yy * width + xx] == isInside) {
                            continue;
                        }
                        int dx = xx - x;
                        int dy = yy - y;
                        int distSq = dx * dx + dy * dy;
                        if (distSq < bestSq) {
                            bestSq = distSq;
                        }
                    }
                }

                float dist = (float) Math.sqrt(bestSq);
                float signed = isInside ? dist : -dist;
                float normalized = 0.5f + (signed / SDF_SPREAD) * 0.5f;
                if (normalized < 0f) normalized = 0f;
                if (normalized > 1f) normalized = 1f;
                int alpha = (int) (normalized * 255f);
                int argb = (alpha << 24) | 0xFFFFFF;
                dst.setRGB(x, y, argb);
            }
        }
        return dst;
    }

    private Atlas uploadAtlas(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                pixels.put((byte) ((argb >> 16) & 0xFF));
                pixels.put((byte) ((argb >> 8) & 0xFF));
                pixels.put((byte) (argb & 0xFF));
                pixels.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        pixels.flip();

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return new Atlas(tex, width, height);
    }

    private boolean initShader() {
        String vert = "#version 120\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
            "  vUV = gl_MultiTexCoord0.xy;\n" +
            "  gl_FrontColor = gl_Color;\n" +
            "}";
        String frag = "#version 120\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform float uEdge;\n" +
            "uniform float uSmoothing;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  float d = texture2D(uTexture, vUV).a;\n" +
            "  float alpha = smoothstep(uEdge - uSmoothing, uEdge + uSmoothing, d);\n" +
            "  gl_FragColor = vec4(gl_Color.rgb, gl_Color.a * alpha);\n" +
            "}";

        int vs = compileShader(GL20.GL_VERTEX_SHADER, vert);
        int fs = compileShader(GL20.GL_FRAGMENT_SHADER, frag);
        if (vs == 0 || fs == 0) {
            return false;
        }

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteProgram(program);
            return false;
        }

        this.shaderProgram = program;
        this.uniformTexture = GL20.glGetUniformLocation(program, "uTexture");
        this.uniformEdge = GL20.glGetUniformLocation(program, "uEdge");
        this.uniformSmoothing = GL20.glGetUniformLocation(program, "uSmoothing");
        return true;
    }

    private int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static class Atlas {
        private final int textureId;
        private final int width;
        private final int height;

        private Atlas(int textureId, int width, int height) {
            this.textureId = textureId;
            this.width = width;
            this.height = height;
        }
    }

    private static class GlyphInfo {
        private final float u0;
        private final float v0;
        private final float u1;
        private final float v1;
        private final int width;
        private final int height;
        private final float offsetX;
        private final float offsetY;
        private final float advance;

        private GlyphInfo(float u0, float v0, float u1, float v1, int width, int height, float offsetX, float offsetY, float advance) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.advance = advance;
        }
    }

    private static class AtlasBuild {
        private final BufferedImage image;
        private final Map<Character, GlyphInfo> glyphs;
        private final float ascent;
        private final float lineHeight;

        private AtlasBuild(BufferedImage image, Map<Character, GlyphInfo> glyphs, float ascent, float lineHeight) {
            this.image = image;
            this.glyphs = glyphs;
            this.ascent = ascent;
            this.lineHeight = lineHeight;
        }
    }

    private static class AtlasCacheEntry {
        private final Atlas atlas;
        private final Map<Character, GlyphInfo> glyphs;
        private final float ascent;
        private final float lineHeight;

        private AtlasCacheEntry(Atlas atlas, Map<Character, GlyphInfo> glyphs, float ascent, float lineHeight) {
            this.atlas = atlas;
            this.glyphs = glyphs;
            this.ascent = ascent;
            this.lineHeight = lineHeight;
        }
    }

    private static class GlyphPlacement {
        private final int x;
        private final int y;
        private final Rectangle bounds;
        private final int advance;
        private final GlyphMetrics metrics;

        private GlyphPlacement(int x, int y, Rectangle bounds, int advance, GlyphMetrics metrics) {
            this.x = x;
            this.y = y;
            this.bounds = bounds;
            this.advance = advance;
            this.metrics = metrics;
        }
    }
}
