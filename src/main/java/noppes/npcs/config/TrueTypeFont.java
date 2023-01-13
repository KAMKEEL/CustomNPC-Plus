//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.util.LRUHashMap;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public class TrueTypeFont {
    private static final int MaxWidth = 512;
    private static final List<Font> allFonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
    private List<Font> usedFonts = new ArrayList();
    private LinkedHashMap<String, TrueTypeFont.GlyphCache> textcache = new LRUHashMap(100);
    private Map<Character, TrueTypeFont.Glyph> glyphcache = new HashMap();
    private List<TrueTypeFont.TextureCache> textures = new ArrayList();
    private Font font;
    private int lineHeight = 1;
    private Graphics2D globalG = (Graphics2D)(new BufferedImage(1, 1, 2)).getGraphics();
    public float scale = 1.0F;
    private int specialChar = 167;

    public TrueTypeFont(Font font, float scale) {
        this.font = font;
        this.scale = scale;
        this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
    }

    public TrueTypeFont(ResourceLocation resource, int fontSize, float scale) throws IOException, FontFormatException {
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font font = Font.createFont(0, stream);
        ge.registerFont(font);
        this.font = font.deriveFont(0, (float)fontSize);
        this.scale = scale;
        this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
    }

    public void setSpecial(char c) {
        this.specialChar = c;
    }

    public void draw(String text, float x, float y, int color) {
        TrueTypeFont.GlyphCache cache = this.getOrCreateCache(text);
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        GL11.glColor4f(r, g, b, 1.0F);
        //GlStateManager.func_179147_l();
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(this.scale, this.scale, 1.0F);
        float i = 0.0F;
        Iterator var10 = cache.glyphs.iterator();

        while(var10.hasNext()) {
            TrueTypeFont.Glyph gl = (TrueTypeFont.Glyph)var10.next();
            if(gl.type != TrueTypeFont.GlyphType.NORMAL) {
                if(gl.type == TrueTypeFont.GlyphType.RESET) {
                    GL11.glColor4f(r, g, b, 1.0F);
                } else if(gl.type == TrueTypeFont.GlyphType.COLOR) {
                    GL11.glColor4f((float) (gl.color >> 16 & 255) / 255.0F, (float) (gl.color >> 8 & 255) / 255.0F, (float) (gl.color & 255) / 255.0F, 1.0F);
                }
            } else {
                GL11.glBindTexture(gl.texture, gl.texture);
                this.drawTexturedModalRect(i, 0.0F, (float)gl.x * this.textureScale(), (float)gl.y * this.textureScale(), (float)gl.width * this.textureScale(), (float)gl.height * this.textureScale());
                i += (float)gl.width * this.textureScale();
            }
        }

        //GlStateManager.func_179084_k();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private TrueTypeFont.GlyphCache getOrCreateCache(String text) {
        TrueTypeFont.GlyphCache cache = (TrueTypeFont.GlyphCache)this.textcache.get(text);
        if(cache != null) {
            return cache;
        } else {
            cache = new TrueTypeFont.GlyphCache();

            for(int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if(c == this.specialChar && i + 1 < text.length()) {
                    char g = text.toLowerCase(Locale.ENGLISH).charAt(i + 1);
                    int index = "0123456789abcdefklmnor".indexOf(g);
                    if(index >= 0) {
                        TrueTypeFont.Glyph g1 = new TrueTypeFont.Glyph();
                        if(index < 16) {
                            g1.type = TrueTypeFont.GlyphType.COLOR;
                            //g1.color = Minecraft.getMinecraft().fontRenderer.func_175064_b(g);
                            g1.color = 0;
                        } else if(index == 16) {
                            g1.type = TrueTypeFont.GlyphType.RANDOM;
                        } else if(index == 17) {
                            g1.type = TrueTypeFont.GlyphType.BOLD;
                        } else if(index == 18) {
                            g1.type = TrueTypeFont.GlyphType.STRIKETHROUGH;
                        } else if(index == 19) {
                            g1.type = TrueTypeFont.GlyphType.UNDERLINE;
                        } else if(index == 20) {
                            g1.type = TrueTypeFont.GlyphType.ITALIC;
                        } else {
                            g1.type = TrueTypeFont.GlyphType.RESET;
                        }

                        cache.glyphs.add(g1);
                        ++i;
                        continue;
                    }
                }

                TrueTypeFont.Glyph var8 = this.getOrCreateGlyph(c);
                cache.glyphs.add(var8);
                cache.width += var8.width;
                cache.height = Math.max(cache.height, var8.height);
            }

            this.textcache.put(text, cache);
            return cache;
        }
    }

    private TrueTypeFont.Glyph getOrCreateGlyph(char c) {
        TrueTypeFont.Glyph g = (TrueTypeFont.Glyph)this.glyphcache.get(Character.valueOf(c));
        if(g != null) {
            return g;
        } else {
            TrueTypeFont.TextureCache cache = this.getCurrentTexture();
            Font font = this.getFontForChar(c);
            FontMetrics metrics = this.globalG.getFontMetrics(font);
            g = new TrueTypeFont.Glyph();
            g.width = Math.max(metrics.charWidth(c), 1);
            g.height = Math.max(metrics.getHeight(), 1);
            if(cache.x + g.width >= 512) {
                cache.x = 0;
                cache.y += this.lineHeight + 1;
                if(cache.y >= 512) {
                    cache.full = true;
                    cache = this.getCurrentTexture();
                }
            }

            g.x = cache.x;
            g.y = cache.y;
            cache.x += g.width + 3;
            this.lineHeight = Math.max(this.lineHeight, g.height);
            cache.g.setFont(font);
            cache.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            cache.g.drawString(c + "", g.x, g.y + metrics.getAscent());
            g.texture = cache.textureId;
            TextureUtil.uploadTextureImage(cache.textureId, cache.bufferedImage);
            this.glyphcache.put(Character.valueOf(c), g);
            return g;
        }
    }

    private TrueTypeFont.TextureCache getCurrentTexture() {
        TrueTypeFont.TextureCache cache = null;
        Iterator var2 = this.textures.iterator();

        while(var2.hasNext()) {
            TrueTypeFont.TextureCache t = (TrueTypeFont.TextureCache)var2.next();
            if(!t.full) {
                cache = t;
                break;
            }
        }

        if(cache == null) {
            this.textures.add(cache = new TrueTypeFont.TextureCache());
        }

        return cache;
    }

    public void drawCentered(String text, float x, float y, int color) {
        this.draw(text, x - (float)this.width(text) / 2.0F, y, color);
    }

    private Font getFontForChar(char c) {
        if(this.font.canDisplay(c)) {
            return this.font;
        } else {
            Iterator fa = this.usedFonts.iterator();

            Font f;
            do {
                if(!fa.hasNext()) {
                    Font fa1 = new Font("Arial Unicode MS", 0, this.font.getSize());
                    if(fa1.canDisplay(c)) {
                        return fa1;
                    }

                    Iterator f2 = allFonts.iterator();

                    Font f1;
                    do {
                        if(!f2.hasNext()) {
                            return this.font;
                        }

                        f1 = (Font)f2.next();
                    } while(!f1.canDisplay(c));

                    this.usedFonts.add(f1 = f1.deriveFont(0, (float)this.font.getSize()));
                    return f1;
                }

                f = (Font)fa.next();
            } while(!f.canDisplay(c));

            return f;
        }
    }

    public void drawTexturedModalRect(float p_73729_1_, float p_73729_2_, float p_73729_3_, float p_73729_4_, float p_73729_5_, float p_73729_6_)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        byte zLevel = 0;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(p_73729_1_ + 0), (double)(p_73729_2_ + p_73729_6_), (double)zLevel, (double)((float)(p_73729_3_ + 0) * f), (double)((float)(p_73729_4_ + p_73729_6_) * f1));
        tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_ + p_73729_6_), (double)zLevel, (double)((float)(p_73729_3_ + p_73729_5_) * f), (double)((float)(p_73729_4_ + p_73729_6_) * f1));
        tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_ + 0), (double)zLevel, (double)((float)(p_73729_3_ + p_73729_5_) * f), (double)((float)(p_73729_4_ + 0) * f1));
        tessellator.addVertexWithUV((double)(p_73729_1_ + 0), (double)(p_73729_2_ + 0), (double)zLevel, (double)((float)(p_73729_3_ + 0) * f), (double)((float)(p_73729_4_ + 0) * f1));
        tessellator.draw();
    }

    public int width(String text) {
        TrueTypeFont.GlyphCache cache = this.getOrCreateCache(text);
        return (int)((float)cache.width * this.scale * this.textureScale());
    }

    public int height(String text) {
        if(text != null && !text.trim().isEmpty()) {
            TrueTypeFont.GlyphCache cache = this.getOrCreateCache(text);
            return Math.max(1, (int)((float)cache.height * this.scale * this.textureScale()));
        } else {
            return (int)((float)this.lineHeight * this.scale * this.textureScale());
        }
    }

    private float textureScale() {
        return 0.5F;
    }

    public void dispose() {
        Iterator var1 = this.textures.iterator();

        while(var1.hasNext()) {
            TrueTypeFont.TextureCache cache = (TrueTypeFont.TextureCache)var1.next();
            GL11.glDeleteTextures(cache.textureId);
        }

        this.textcache.clear();
    }

    public String getFontName() {
        return this.font.getFontName();
    }

    class GlyphCache {
        public int width;
        public int height;
        List<TrueTypeFont.Glyph> glyphs = new ArrayList();

        GlyphCache() {
        }
    }

    class Glyph {
        TrueTypeFont.GlyphType type;
        int color;
        int x;
        int y;
        int height;
        int width;
        int texture;

        Glyph() {
            this.type = TrueTypeFont.GlyphType.NORMAL;
            this.color = -1;
        }
    }

    class TextureCache {
        int x;
        int y;
        int textureId = GL11.glGenTextures();
        BufferedImage bufferedImage = new BufferedImage(512, 512, 2);
        Graphics2D g;
        boolean full;

        TextureCache() {
            this.g = (Graphics2D)this.bufferedImage.getGraphics();
        }
    }

    static enum GlyphType {
        NORMAL,
        COLOR,
        RANDOM,
        BOLD,
        STRIKETHROUGH,
        UNDERLINE,
        ITALIC,
        RESET,
        OTHER;

        private GlyphType() {
        }
    }
}
