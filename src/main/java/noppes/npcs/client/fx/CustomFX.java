package noppes.npcs.client.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CustomFX extends EntityFX {
    private final Entity entity;
    private final ResourceLocation location;
    private static final ResourceLocation resource = new ResourceLocation("textures/particle/particles.png");
    private float startX = 0, startY = 0, startZ = 0;

    public float scale1 = 1.0F;
    public float scale2 = 1.0F;
    public float scaleRate = 1.0F;
    public int scaleRateStart = 0;

    public float alpha1 = 1.0F;
    public float alpha2 = 0;
    public float alphaRate = 0.0F;
    public int alphaRateStart = 0;

    public float rotationX = 0;
    public float rotationX1 = 0;
    public float rotationX2 = 0;
    public float rotationXRate = 0.0F;
    public int rotationXRateStart = 0;

    public float rotationY = 0;
    public float rotationY1 = 0;
    public float rotationY2 = 0;
    public float rotationYRate = 0.0F;
    public int rotationYRateStart = 0;

    public float rotationZ = 0;
    public float rotationZ1 = 0;
    public float rotationZ2 = 0;
    public float rotationZRate = 0.0F;
    public int rotationZRateStart = 0;

    public boolean facePlayer = true;
    public boolean glows = true;

    private int totalWidth, totalHeight;
    public int width, height;
    public int offsetX, offsetY;

    private int timeSinceStart;
    public int animRate;
    public boolean animLoop;
    public int animStart,animEnd;
    private int animPosX;
    private int animPosY;

    public int HEXColor = 0xFFFFFF;
    public int HEXColor2 = 0xFFFFFF;
    public float HEXColorRate = 0.0F;
    public int HEXColorStart = 0;

    private ImageDownloadAlt imageDownloadAlt = null;
    private boolean isUrl = false;
    private boolean gotWidthHeight = false;

    double renderPosX, renderPosY, renderPosZ;

	public CustomFX(World worldObj, Entity entity, String directory,
                    int HEXColor, int HEXColor2, float HEXColorRate, int HEXColorStart,
                    double x, double y, double z,
                    double motionX, double motionY, double motionZ, float gravity,
                    float scale1, float scale2, float scaleRate, int scaleRateStart,
                    float alpha1, float alpha2, float alphaRate, int alphaRateStart,
                    float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart,
                    float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart,
                    float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart,
                    int age, boolean facePlayer, boolean glows, int width, int height, int offsetX, int offsetY,
                    int animRate, boolean animLoop, int animStart, int animEnd
    ) {
		super(worldObj, x, y, z, motionX, motionY, motionZ);

        this.HEXColor = HEXColor;
        this.HEXColor2 = HEXColor2;
        this.HEXColorRate = HEXColorRate;
        this.HEXColorStart = HEXColorStart;
        particleRed = (HEXColor >> 16 & 255) / 255f;
        particleGreen = (HEXColor >> 8  & 255) / 255f;
        particleBlue = (HEXColor & 255) / 255f;

        this.scale1 = scale1;
        this.scale2 = scale2;
        this.scaleRate = Math.abs(scaleRate);
        this.scaleRateStart = scaleRateStart;
        particleScale = scale1;
        if(scale1 > scale2)
            this.scaleRate *= -1;

        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.alphaRate = Math.abs(alphaRate);
        this.alphaRateStart = alphaRateStart;
        particleAlpha = alpha1;
        if(alpha1 > alpha2)
            this.alphaRate *= -1;

        this.rotationX1 = rotationX1;
        this.rotationX2 = rotationX2;
        this.rotationXRate = Math.abs(rotationXRate);
        this.rotationXRateStart = rotationXRateStart;
        rotationX = rotationX1;
        if(rotationX1 > rotationX2)
            this.rotationXRate *= -1;

        this.rotationY1 = rotationY1;
        this.rotationY2 = rotationY2;
        this.rotationYRate = Math.abs(rotationYRate);
        this.rotationYRateStart = rotationYRateStart;
        rotationY = rotationY1;
        if(rotationY1 > rotationY2)
            this.rotationYRate *= -1;

        this.rotationZ1 = rotationZ1;
        this.rotationZ2 = rotationZ2;
        this.rotationZRate = Math.abs(rotationZRate);
        this.rotationZRateStart = rotationZRateStart;
        rotationZ = rotationZ1;
        if(rotationZ1 > rotationZ2)
            this.rotationZRate *= -1;

        particleGravity = gravity/0.04F;
        particleMaxAge = age;

        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;

		this.entity = entity;
        noClip = true;

        location = new ResourceLocation(directory);

        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        this.animRate = animRate;
        this.animLoop = animLoop;
        this.animStart = animStart;
        this.animEnd = animEnd;
        if(this.animEnd < this.animStart)
            this.animEnd = this.animStart + this.particleMaxAge;

        if(directory.startsWith("https://")){
            isUrl = true;
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            imageDownloadAlt = new ImageDownloadAlt(null, directory, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
            texturemanager.loadTexture(this.location, imageDownloadAlt);
        } else {
            try {
                getWidthHeight();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.facePlayer = facePlayer;
        this.glows = glows;
	}

    @Override
    public void onUpdate() {
        ++this.timeSinceStart;

        if (this.timeSinceStart == this.particleMaxAge)
        {
            this.setDead();
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.04D * (double)this.particleGravity;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if(animRate > 0 && timeSinceStart%animRate == 0 && timeSinceStart > 0 && gotWidthHeight && timeSinceStart >= animStart && timeSinceStart <= animEnd && (animLoop || animPosY <= totalHeight)){
            animPosX += width;
            if (animPosX + width > totalWidth) {
                animPosX = offsetX;
                animPosY += height;
                if (animPosY > totalHeight && animLoop) {
                    animPosY = offsetY;
                }
            }
        }

        particleRed = (HEXColor >> 16 & 255) / 255f;
        particleGreen = (HEXColor >> 8  & 255) / 255f;
        particleBlue = (HEXColor & 255) / 255f;

        if(timeSinceStart >= HEXColorStart)
            HEXColor = lerpColor(HEXColor, HEXColor2, HEXColorRate);
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTick, float cosYaw, float cosPitch, float sinYaw, float sinSinPitch, float cosSinPitch)
    {
        tessellator.draw();
        ClientProxy.bindTexture(location);

        if(imageDownloadAlt != null && isUrl && !gotWidthHeight){
            getURLWidthHeight();
        }

        if(entity != null){
            startX = (float)(entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTick);
            startY = (float)(entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTick);
            startZ = (float)(entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTick);
        }

        float scaleChange = this.scaleRate / (float)particleMaxAge;
        if((this.scaleRate < 0 && particleScale+scaleChange < this.scale2) || (this.scaleRate > 0 && particleScale+scaleChange > this.scale2))
            particleScale = this.scale2;
        else if(timeSinceStart >= this.scaleRateStart)
            particleScale += scaleChange;

        float alphaChange = this.alphaRate / (float)particleMaxAge;
        if((this.alphaRate < 0 && particleAlpha+alphaChange < this.alpha2) || (this.alphaRate > 0 && particleAlpha+alphaChange > this.alpha2))
            particleAlpha = this.alpha2;
        else if(timeSinceStart >= this.alphaRateStart)
            particleAlpha += alphaChange;

        float rotationXChange = this.rotationXRate / (float)particleMaxAge;
        if((this.rotationXRate < 0 && rotationX+rotationXChange < this.rotationX2) || (this.rotationXRate > 0 && rotationX+rotationXChange > this.rotationX2))
            rotationX = this.rotationX2;
        else if(timeSinceStart >= this.rotationXRateStart)
            rotationX += rotationXChange;

        float rotationYChange = this.rotationYRate / (float)particleMaxAge;
        if((this.rotationYRate < 0 && rotationY+rotationYChange < this.rotationY2) || (this.rotationYRate > 0 && rotationY+rotationYChange > this.rotationY2))
            rotationY = this.rotationY2;
        else if(timeSinceStart >= this.rotationYRateStart)
            rotationY += rotationYChange;

        float rotationZChange = this.rotationZRate / (float)particleMaxAge;
        if((this.rotationZRate < 0 && rotationZ+rotationZChange < this.rotationZ2) || (this.rotationZRate > 0 && rotationZ+rotationZChange > this.rotationZ2))
            rotationZ = this.rotationZ2;
        else if(timeSinceStart >= this.rotationZRateStart)
            rotationZ += rotationZChange;

        renderParticleSide(true, tessellator, partialTick);
        renderParticleSide(false, tessellator, partialTick);

        GL11.glColor4f(1, 1, 1, 1.0F);
        ClientProxy.bindTexture(resource);
        tessellator.startDrawingQuads();
    }

    public void renderParticleSide(boolean front, Tessellator tessellator, float partialTick){
        float u1 = (float)offsetX/(float)totalWidth + (float)animPosX/(float)totalWidth;
        float u2 = u1 + (float)width/(float)totalWidth;
        float v1 = (float)offsetY/(float)totalHeight + (float)animPosY/(float)totalHeight;
        float v2 = v1 + (float)height/(float)totalHeight;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        float renderScale = 0.1F * particleScale;
        float posX = (float)((prevPosX + (this.posX - prevPosX) * (double)partialTick) - interpPosX) + startX;
        float posY = (float)((prevPosY + (this.posY - prevPosY) * (double)partialTick) - interpPosY) + startY;
        float posZ = (float)((prevPosZ + (this.posZ - prevPosZ) * (double)partialTick) - interpPosZ) + startZ;

        GL11.glPushMatrix();
            renderPosX = startX + posX + this.posX;
            renderPosY = startY + posY + this.posY;
            renderPosZ = startZ + posZ + this.posZ;

            GL11.glTranslated(posX,posY,posZ);
            GL11.glScalef(renderScale, renderScale, renderScale);
            if(facePlayer) {
                GL11.glRotated(180 - player.rotationYaw, 0.0, 1.0, 0.0);
                GL11.glRotated(-player.rotationPitch + 90, 1.0, 0.0, 0.0);

                GL11.glRotated(rotationX, 1.0, 0.0, 0.0);
                GL11.glRotated(rotationZ, 0.0, 1.0, 0.0);
                GL11.glRotated(rotationY + (!front ? 180 : 0), 0.0, 0.0, 1.0);
            } else {
                GL11.glRotated(rotationX, 1.0, 0.0, 0.0);
                GL11.glRotated(rotationY, 0.0, 1.0, 0.0);
                GL11.glRotated(rotationZ + (!front ? 180 : 0), 0.0, 0.0, 1.0);
            }

            tessellator.startDrawingQuads();
            if (!this.glows) {
                tessellator.setBrightness(this.getBrightnessForRender(partialTick));
            } else {
                tessellator.setBrightness(240);
            }
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
            tessellator.addVertexWithUV( (u2-u1)/2,  0,  (v2-v1)/2, u2, v2);
            tessellator.addVertexWithUV( (u2-u1)/2,  0, -(v2-v1)/2, u2, v1);
            tessellator.addVertexWithUV( -(u2-u1)/2,  0,  -(v2-v1)/2, u1, v1);
            tessellator.addVertexWithUV( -(u2-u1)/2,  0, (v2-v1)/2, u1, v2);
            tessellator.draw();
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float p_70070_1_)
    {
        int i = MathHelper.floor_double(renderPosX);
        int j = MathHelper.floor_double(renderPosZ);

        if (this.worldObj.blockExists(i, 0, j))
        {
            double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D;
            int k = MathHelper.floor_double(renderPosY - (double)this.yOffset + d0);
            return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
        }
        else
        {
            return 0;
        }
    }

    public void getWidthHeight() throws IOException {
        InputStream inputstream = null;

        try {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            inputstream = iresource.getInputStream();
            BufferedImage bufferedimage = ImageIO.read(inputstream);
            gotWidthHeight = true;
            this.totalWidth = bufferedimage.getWidth();
            this.totalHeight = bufferedimage.getHeight();
            correctWidthHeight();
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
    }

    public void getURLWidthHeight(){
        if(imageDownloadAlt.getBufferedImage() != null) {
            gotWidthHeight = true;
            this.totalWidth = imageDownloadAlt.getBufferedImage().getWidth();
            this.totalHeight = imageDownloadAlt.getBufferedImage().getHeight();
            correctWidthHeight();
        }
    }

    public void correctWidthHeight(){
        totalWidth = Math.max(totalWidth, 1);
        totalHeight = Math.max(totalHeight, 1);
        this.width = width < 0 ? totalWidth : width;
        this.height = height < 0 ? totalHeight : height;
    }

    public int lerpColor(int from, int to, float ratio) {
        float ar = (from & 0xFF0000) >> 16;
        float ag = (from & 0x00FF00) >> 8;
        float ab = (from & 0x0000FF);

        float br = (to & 0xFF0000) >> 16;
        float bg = (to & 0x00FF00) >> 8;
        float bb = (to & 0x0000FF);

        float rr = ar + ratio * (br - ar);
        float rg = ag + ratio * (bg - ag);
        float rb = ab + ratio * (bb - ab);

        return (int)(((int)rr << 16) + ((int)rg << 8) + (rb));
    }
    
    public int getFXLayer(){
    	return 0;
    }
}
