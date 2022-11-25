package noppes.npcs.client.gui.customoverlay.components;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.api.overlay.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.ScriptOverlayLabel;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CustomOverlayLabel extends Gui implements IOverlayComponent {
    OverlayCustom parent;
    int id;

    String fullLabel;
    int x, y, width, height, alignment;
    float scale, rotation;
    int color;
    float alpha;

    int border = 2;
    boolean labelBgEnabled = true;
    boolean labelShadowEnabled = false;

    protected int field_146167_a;
    protected int field_146161_f;
    public int field_146162_g;
    public int field_146174_h;

    private boolean randomStyle = false;
    private boolean boldStyle = false;
    private boolean italicStyle = false;
    private boolean underlineStyle = false;
    private boolean strikethroughStyle = false;
    private boolean bidiFlag = false;
    private boolean unicodeFlag = false;

    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    protected final ResourceLocation locationFontTexture;
    protected int[] charWidth = new int[256];
    protected byte[] glyphWidth = new byte[65536];
    private final int[] colorCode = new int[32];

    private float red;
    private float green;
    private float blue;

    private float posX;
    private float posY;

    public CustomOverlayLabel(int id, String fullLabel, int x, int y, int width, int height, boolean shadow){
        this.id = id;
        this.fullLabel = fullLabel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labelShadowEnabled = shadow;

        locationFontTexture = new ResourceLocation("textures/font/ascii.png");
        readFontTexture();
        readGlyphSizes();
        for (int i = 0; i < 32; ++i)
        {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;

            if (i == 6)
            {
                k += 85;
            }

            if (Minecraft.getMinecraft().gameSettings.anaglyph)
            {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16)
            {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    public void setParent(OverlayCustom parent) {
        this.parent = parent;
    }

    public void onRender(Minecraft mc, float partialTicks) {
        this.drawLabel();
    }

    public int getID() {
        return this.id;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public static CustomOverlayLabel fromComponent(ScriptOverlayLabel component) {
        CustomOverlayLabel lbl = new CustomOverlayLabel(component.getID(), component.getText(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getShadow());
        lbl.scale = 1.0F;
        lbl.color = component.getColor();
        lbl.labelShadowEnabled = component.getShadow();
        lbl.alignment = component.getAlignment();
        lbl.alpha = component.getAlpha();
        lbl.rotation = component.getRotation();
        lbl.setScale(component.getScale());

        return lbl;
    }

    public ICustomOverlayComponent toComponent() {
        ScriptOverlayLabel component = new ScriptOverlayLabel(this.id, this.fullLabel, this.field_146162_g, this.field_146174_h, this.field_146167_a, this.field_146161_f, this.color);
        component.setShadow(this.labelShadowEnabled);
        component.setAlignment(alignment);
        component.setAlpha(alpha);
        component.setColor(color);
        component.setRotation(rotation);

        return component;
    }

    public void drawLabel()
    {
        GL11.glPushMatrix();
            GL11.glTranslatef(this.alignment%3*((float)(OverlayCustom.scaledWidth)/2), (float) (Math.floor((float)(alignment/3))*((float)(OverlayCustom.scaledHeight)/2)),0.0F);//alignment%3 * width/2  Math.floor(alignment/3) * height/2

            red = (color >> 16 & 255) / 255f;
            green = (color >> 8  & 255) / 255f;
            blue = (color & 255) / 255f;

            GL11.glTranslatef(this.x,this.y,0.0F);
            GL11.glRotated(rotation,0.0D,0.0D,1.0D);
            GL11.glScalef(this.scale, this.scale, this.scale);
            this.drawString(fullLabel, 0, 0, this.color, this.labelShadowEnabled);
        GL11.glPopMatrix();
    }

    public int drawString(String p_85187_1_, int p_85187_2_, int p_85187_3_, int p_85187_4_, boolean p_85187_5_)
    {
        this.resetStyles();
        int l;

        if (p_85187_5_)
        {
            l = this.renderString(p_85187_1_, p_85187_2_ + 1, p_85187_3_ + 1, p_85187_4_, true);
            l = Math.max(l, this.renderString(p_85187_1_, p_85187_2_, p_85187_3_, p_85187_4_, false));
        }
        else
        {
            l = this.renderString(p_85187_1_, p_85187_2_, p_85187_3_, p_85187_4_, false);
        }

        return l;
    }

    private void resetStyles()
    {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private int renderString(String p_78258_1_, int p_78258_2_, int p_78258_3_, int p_78258_4_, boolean p_78258_5_)
    {
        if (p_78258_1_ == null)
        {
            return 0;
        }
        else
        {
            if (this.bidiFlag)
            {
                p_78258_1_ = this.bidiReorder(p_78258_1_);
            }

            setColor(this.red, this.green, this.blue, this.alpha);
            this.posX = (float)p_78258_2_;
            this.posY = (float)p_78258_3_;
            this.renderStringAtPos(p_78258_1_, p_78258_5_);
            return (int)this.posX;
        }
    }

    private void renderStringAtPos(String p_78255_1_, boolean p_78255_2_)
    {
        for (int i = 0; i < p_78255_1_.length(); ++i)
        {
            char c0 = p_78255_1_.charAt(i);
            int j;
            int k;

            if (c0 == 38 && i + 1 < p_78255_1_.length())
            {
                j = "0123456789abcdefklmnor".indexOf(p_78255_1_.toLowerCase().charAt(i + 1));

                if(j < 16){
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (j < 0 || j > 15)
                    {
                        j = 15;
                    }

                    if (p_78255_2_)
                    {
                        j += 16;
                    }

                    k = this.colorCode[j];
                    setColor((float)(k >> 16) / 255.0F, (float)(k >> 8 & 255) / 255.0F, (float)(k & 255) / 255.0F, this.alpha);
                }
                else if (j == 16)
                {
                    this.randomStyle = true;
                }
                else if (j == 17)
                {
                    this.boldStyle = true;
                }
                else if (j == 18)
                {
                    this.strikethroughStyle = true;
                }
                else if (j == 19)
                {
                    this.underlineStyle = true;
                }
                else if (j == 20)
                {
                    this.italicStyle = true;
                }
                else if (j == 21)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                }

                ++i;
            }
            else
            {
                j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c0);

                if (this.randomStyle && j != -1)
                {
                    do
                    {
                        k = Minecraft.getMinecraft().fontRenderer.fontRandom.nextInt(this.charWidth.length);
                    }
                    while (this.charWidth[j] != this.charWidth[k]);

                    j = k;
                }

                float f1 = this.unicodeFlag ? 0.5F : 1.0F;
                boolean flag1 = (c0 == 0 || j == -1 || this.unicodeFlag) && p_78255_2_;

                if (flag1)
                {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderCharAtPos(j, c0, this.italicStyle);

                if (flag1)
                {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle)
                {
                    this.posX += f1;

                    if (flag1)
                    {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderCharAtPos(j, c0, this.italicStyle);
                    this.posX -= f1;

                    if (flag1)
                    {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    ++f;
                }

                doDraw(f);
            }
        }
    }

    protected void doDraw(float f)
    {
        {
            {
                Tessellator tessellator;

                if (this.strikethroughStyle)
                {
                    tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2)), 0.0D);
                    tessellator.addVertex((double)(this.posX + f), (double)(this.posY + (float)(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2)), 0.0D);
                    tessellator.addVertex((double)(this.posX + f), (double)(this.posY + (float)(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                if (this.underlineStyle)
                {
                    tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    int l = this.underlineStyle ? -1 : 0;
                    tessellator.addVertex((double)(this.posX + (float)l), (double)(this.posY + (float)Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT), 0.0D);
                    tessellator.addVertex((double)(this.posX + f), (double)(this.posY + (float)Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT), 0.0D);
                    tessellator.addVertex((double)(this.posX + f), (double)(this.posY + (float)Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.addVertex((double)(this.posX + (float)l), (double)(this.posY + (float)Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                this.posX += (float)((int)f);
            }
        }
    }

    private float renderCharAtPos(int p_78278_1_, char p_78278_2_, boolean p_78278_3_)
    {
        return p_78278_2_ == 32 ? 4.0F : ("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(p_78278_2_) != -1 && !this.unicodeFlag ? this.renderDefaultChar(p_78278_1_, p_78278_3_) : this.renderUnicodeChar(p_78278_2_, p_78278_3_));
    }

    protected float renderDefaultChar(int p_78266_1_, boolean p_78266_2_)
    {
        float f = (float)(p_78266_1_ % 16 * 8);
        float f1 = (float)(p_78266_1_ / 16 * 8);
        float f2 = p_78266_2_ ? 1.0F : 0.0F;
        bindTexture(this.locationFontTexture);
        float f3 = (float)this.charWidth[p_78266_1_] - 0.01F;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(f / 128.0F, f1 / 128.0F);
        GL11.glVertex3f(this.posX + f2, this.posY, 0.0F);
        GL11.glTexCoord2f(f / 128.0F, (f1 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - f2, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f((f + f3 - 1.0F) / 128.0F, f1 / 128.0F);
        GL11.glVertex3f(this.posX + f3 - 1.0F + f2, this.posY, 0.0F);
        GL11.glTexCoord2f((f + f3 - 1.0F) / 128.0F, (f1 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + f3 - 1.0F - f2, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return (float)this.charWidth[p_78266_1_];
    }

    private ResourceLocation getUnicodePageLocation(int p_111271_1_)
    {
        if (unicodePageLocations[p_111271_1_] == null)
        {
            unicodePageLocations[p_111271_1_] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", new Object[] {Integer.valueOf(p_111271_1_)}));
        }

        return unicodePageLocations[p_111271_1_];
    }

    private void loadGlyphTexture(int p_78257_1_)
    {
        bindTexture(this.getUnicodePageLocation(p_78257_1_));
    }

    /**
     * Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files...
     */
    protected float renderUnicodeChar(char p_78277_1_, boolean p_78277_2_)
    {
        if (this.glyphWidth[p_78277_1_] == 0)
        {
            return 0.0F;
        }
        else
        {
            int i = p_78277_1_ / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[p_78277_1_] >>> 4;
            int k = this.glyphWidth[p_78277_1_] & 15;
            float f = (float)j;
            float f1 = (float)(k + 1);
            float f2 = (float)(p_78277_1_ % 16 * 16) + f;
            float f3 = (float)((p_78277_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_78277_2_ ? 1.0F : 0.0F;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    protected void setColor(float r, float g, float b, float a)
    {
        GL11.glColor4f(r, g, b, a);
    }

    protected void bindTexture(ResourceLocation location)
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
    }

    private void readFontTexture()
    {
        BufferedImage bufferedimage;

        try
        {
            bufferedimage = ImageIO.read(getResourceInputStream(this.locationFontTexture));
        }
        catch (IOException ioexception)
        {
            throw new RuntimeException(ioexception);
        }

        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int[] aint = new int[i * j];
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
        int k = j / 16;
        int l = i / 16;
        byte b0 = 1;
        float f = 8.0F / (float)l;
        int i1 = 0;

        while (i1 < 256)
        {
            int j1 = i1 % 16;
            int k1 = i1 / 16;

            if (i1 == 32)
            {
                this.charWidth[i1] = 3 + b0;
            }

            int l1 = l - 1;

            while (true)
            {
                if (l1 >= 0)
                {
                    int i2 = j1 * l + l1;
                    boolean flag = true;

                    for (int j2 = 0; j2 < k && flag; ++j2)
                    {
                        int k2 = (k1 * l + j2) * i;

                        if ((aint[i2 + k2] >> 24 & 255) != 0)
                        {
                            flag = false;
                        }
                    }

                    if (flag)
                    {
                        --l1;
                        continue;
                    }
                }

                ++l1;
                this.charWidth[i1] = (int)(0.5D + (double)((float)l1 * f)) + b0;
                ++i1;
                break;
            }
        }
    }

    private void readGlyphSizes()
    {
        try
        {
            InputStream inputstream = getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
            inputstream.read(this.glyphWidth);
        }
        catch (IOException ioexception)
        {
            throw new RuntimeException(ioexception);
        }
    }

    protected InputStream getResourceInputStream(ResourceLocation location) throws IOException
    {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }

    private String bidiReorder(String p_147647_1_)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_147647_1_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicshapingexception)
        {
            return p_147647_1_;
        }
    }
}
