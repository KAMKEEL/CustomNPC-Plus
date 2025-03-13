package noppes.npcs.client.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.scripted.ScriptParticle;
import org.lwjgl.opengl.GL11;

public class CustomFX extends EntityFX {
    protected final Entity entity;
    protected final String directory;
    protected final ImageData imageData;
    protected static final ResourceLocation resource = new ResourceLocation("textures/particle/particles.png");
    protected float startX = 0, startY = 0, startZ = 0;

    public float scaleX1 = 1.0F;
    public float scaleX2 = 1.0F;
    public float scaleXRate = 1.0F;
    public int scaleXRateStart = 0;

    public float scaleY1 = 1.0F;
    public float scaleY2 = 1.0F;
    public float scaleYRate = 1.0F;
    public int scaleYRateStart = 0;

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

    public int width, height;
    public int offsetX, offsetY;

    protected int timeSinceStart;
    public int animRate;
    public boolean animLoop;
    public int animStart, animEnd;
    protected int animPosX;
    protected int animPosY;

    public int HEXColor = 0xFFFFFF;
    public int HEXColor2 = 0xFFFFFF;
    public float HEXColorRate = 0.0F;
    public int HEXColorStart = 0;

    protected double renderPosX, renderPosY, renderPosZ;

    public CustomFX(World worldObj, Entity entity, String directory, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(worldObj, x, y, z, motionX, motionY, motionZ);
        this.entity = entity;
        this.directory = directory;
        this.imageData = ClientCacheHandler.getImageData(this.directory);
    }

    public static CustomFX fromScriptedParticle(ScriptParticle particle, World worldObj, Entity entity) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (entity == player) {
            particle.y -= player.yOffset;
        }
        CustomFX customFX = new CustomFX(worldObj, entity, particle.directory, particle.x, particle.y, particle.z, particle.motionX, particle.motionY, particle.motionZ);

        customFX.HEXColor = particle.HEXColor;
        customFX.HEXColor2 = particle.HEXColor2;
        customFX.HEXColorRate = particle.HEXColorRate;
        customFX.HEXColorStart = particle.HEXColorStart;
        customFX.particleRed = (customFX.HEXColor >> 16 & 255) / 255f;
        customFX.particleGreen = (customFX.HEXColor >> 8 & 255) / 255f;
        customFX.particleBlue = (customFX.HEXColor & 255) / 255f;

        customFX.scaleX1 = particle.scaleX1;
        customFX.scaleX2 = particle.scaleX2;
        customFX.scaleXRate = Math.abs(particle.scaleXRate);
        customFX.scaleXRateStart = particle.scaleXRateStart;
        if (customFX.scaleX1 > customFX.scaleX2)
            customFX.scaleXRate *= -1;

        customFX.scaleY1 = particle.scaleY1;
        customFX.scaleY2 = particle.scaleY2;
        customFX.scaleYRate = Math.abs(particle.scaleYRate);
        customFX.scaleYRateStart = particle.scaleYRateStart;
        if (customFX.scaleY1 > customFX.scaleY2)
            customFX.scaleYRate *= -1;

        customFX.alpha1 = particle.alpha1;
        customFX.alpha2 = particle.alpha2;
        customFX.alphaRate = Math.abs(particle.alphaRate);
        customFX.alphaRateStart = particle.alphaRateStart;
        customFX.particleAlpha = customFX.alpha1;
        if (customFX.alpha1 > customFX.alpha2)
            customFX.alphaRate *= -1;

        customFX.rotationX1 = particle.rotationX1;
        customFX.rotationX2 = particle.rotationX2;
        customFX.rotationXRate = Math.abs(particle.rotationXRate);
        customFX.rotationXRateStart = particle.rotationXRateStart;
        customFX.rotationX = customFX.rotationX1;
        if (customFX.rotationX1 > customFX.rotationX2)
            customFX.rotationXRate *= -1;

        customFX.rotationY1 = particle.rotationY1;
        customFX.rotationY2 = particle.rotationY2;
        customFX.rotationYRate = Math.abs(particle.rotationYRate);
        customFX.rotationYRateStart = particle.rotationYRateStart;
        customFX.rotationY = customFX.rotationY1;
        if (customFX.rotationY1 > customFX.rotationY2)
            customFX.rotationYRate *= -1;

        customFX.rotationZ1 = particle.rotationZ1;
        customFX.rotationZ2 = particle.rotationZ2;
        customFX.rotationZRate = Math.abs(particle.rotationZRate);
        customFX.rotationZRateStart = particle.rotationZRateStart;
        customFX.rotationZ = customFX.rotationZ1;
        if (customFX.rotationZ1 > customFX.rotationZ2)
            customFX.rotationZRate *= -1;

        customFX.particleGravity = particle.gravity / 0.04F;
        customFX.particleMaxAge = particle.maxAge;

        customFX.motionX = particle.motionX;
        customFX.motionY = particle.motionY;
        customFX.motionZ = particle.motionZ;

        customFX.noClip = particle.noClip;

        customFX.width = particle.width;
        customFX.height = particle.height;
        customFX.offsetX = particle.offsetX;
        customFX.offsetY = particle.offsetY;

        customFX.animRate = particle.animRate;
        customFX.animLoop = particle.animLoop;
        customFX.animStart = particle.animStart;
        customFX.animEnd = particle.animEnd;
        if (customFX.animEnd < customFX.animStart)
            customFX.animEnd = customFX.animStart + customFX.particleMaxAge;

        customFX.facePlayer = particle.facePlayer;
        customFX.glows = particle.glows;

        return customFX;
    }

    @Override
    public void onUpdate() {
        if (!this.imageLoaded()) {
            return;
        }

        ++this.timeSinceStart;

        if (this.timeSinceStart == this.particleMaxAge) {
            this.setDead();
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.04D * (double) this.particleGravity;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (animRate > 0 && timeSinceStart % animRate == 0 && timeSinceStart > 0 && timeSinceStart >= animStart && timeSinceStart <= animEnd && (animLoop || animPosY <= imageData.getTotalHeight())) {
            animPosX += width;
            if (animPosX + width > imageData.getTotalWidth()) {
                animPosX = offsetX;
                animPosY += height;
                if (animPosY > imageData.getTotalHeight() && animLoop) {
                    animPosY = offsetY;
                }
            }
        }

        particleRed = (HEXColor >> 16 & 255) / 255f;
        particleGreen = (HEXColor >> 8 & 255) / 255f;
        particleBlue = (HEXColor & 255) / 255f;

        if (timeSinceStart >= HEXColorStart)
            HEXColor = lerpColor(HEXColor, HEXColor2, HEXColorRate);
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTick, float cosYaw, float cosPitch, float sinYaw, float sinSinPitch, float cosSinPitch) {
        if (!this.imageLoaded()) {
            return;
        }

        tessellator.draw();
        imageData.bindTexture();
        if (imageData.invalid()) {
            setDead();
        }

        if (entity != null) {
            startX = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTick);
            startY = (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTick);
            startZ = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTick);
        }

        float scaleXChange = this.scaleXRate / (float) particleMaxAge;
        if ((this.scaleXRate < 0 && this.scaleX1 + scaleXChange < this.scaleX2) || (this.scaleXRate > 0 && this.scaleX1 + scaleXChange > this.scaleX2))
            this.scaleX1 = this.scaleX2;
        else if (timeSinceStart >= this.scaleXRateStart)
            this.scaleX1 += scaleXChange;

        float scaleYChange = this.scaleYRate / (float) particleMaxAge;
        if ((this.scaleYRate < 0 && this.scaleY1 + scaleYChange < this.scaleY2) || (this.scaleYRate > 0 && this.scaleY1 + scaleYChange > this.scaleY2))
            this.scaleY1 = this.scaleY2;
        else if (timeSinceStart >= this.scaleYRateStart)
            this.scaleY1 += scaleYChange;

        float alphaChange = this.alphaRate / (float) particleMaxAge;
        if ((this.alphaRate < 0 && particleAlpha + alphaChange < this.alpha2) || (this.alphaRate > 0 && particleAlpha + alphaChange > this.alpha2))
            particleAlpha = this.alpha2;
        else if (timeSinceStart >= this.alphaRateStart)
            particleAlpha += alphaChange;

        float rotationXChange = this.rotationXRate / (float) particleMaxAge;
        if ((this.rotationXRate < 0 && rotationX + rotationXChange < this.rotationX2) || (this.rotationXRate > 0 && rotationX + rotationXChange > this.rotationX2))
            rotationX = this.rotationX2;
        else if (timeSinceStart >= this.rotationXRateStart)
            rotationX += rotationXChange;

        float rotationYChange = this.rotationYRate / (float) particleMaxAge;
        if ((this.rotationYRate < 0 && rotationY + rotationYChange < this.rotationY2) || (this.rotationYRate > 0 && rotationY + rotationYChange > this.rotationY2))
            rotationY = this.rotationY2;
        else if (timeSinceStart >= this.rotationYRateStart)
            rotationY += rotationYChange;

        float rotationZChange = this.rotationZRate / (float) particleMaxAge;
        if ((this.rotationZRate < 0 && rotationZ + rotationZChange < this.rotationZ2) || (this.rotationZRate > 0 && rotationZ + rotationZChange > this.rotationZ2))
            rotationZ = this.rotationZ2;
        else if (timeSinceStart >= this.rotationZRateStart)
            rotationZ += rotationZChange;

        renderParticleSide(true, tessellator, partialTick);
        renderParticleSide(false, tessellator, partialTick);

        GL11.glColor4f(1, 1, 1, 1.0F);
        ClientProxy.bindTexture(resource);
        tessellator.startDrawingQuads();
    }

    public void renderParticleSide(boolean front, Tessellator tessellator, float partialTick) {
        int totalWidth = imageData.getTotalWidth();
        int totalHeight = imageData.getTotalHeight();

        float u1 = (float) offsetX / (float) totalWidth + (float) animPosX / (float) totalWidth;
        float u2 = u1 + (float) width / (float) totalWidth;
        float v1 = (float) offsetY / (float) totalHeight + (float) animPosY / (float) totalHeight;
        float v2 = v1 + (float) height / (float) totalHeight;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        float posX = (float) ((prevPosX + (this.posX - prevPosX) * (double) partialTick) - interpPosX) + startX;
        float posY = (float) ((prevPosY + (this.posY - prevPosY) * (double) partialTick) - interpPosY) + startY;
        float posZ = (float) ((prevPosZ + (this.posZ - prevPosZ) * (double) partialTick) - interpPosZ) + startZ;

        GL11.glPushMatrix();
        renderPosX = startX + posX + this.posX;
        renderPosY = startY + posY + this.posY;
        renderPosZ = startZ + posZ + this.posZ;

        GL11.glTranslated(posX, posY, posZ);
        if (facePlayer) {
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

        float textureXScale = 1.0F, textureYScale = 1.0F;
        if (totalWidth > totalHeight) {
            textureYScale = (float) totalHeight / totalWidth;
            GL11.glScalef(1 / textureYScale / 2, 1 / textureYScale / 2, 1 / textureYScale / 2);
        } else if (totalHeight > totalWidth) {
            textureXScale = (float) totalWidth / totalHeight;
            GL11.glScalef(1 / textureXScale / 2, 1 / textureXScale / 2, 1 / textureXScale / 2);
        }
        textureXScale *= scaleX1 / 10.0F;
        textureYScale *= scaleY1 / 10.0F;

        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        tessellator.addVertexWithUV(textureXScale * (u2 - u1) / 2, 0, textureYScale * (v2 - v1) / 2, u2, v2);
        tessellator.addVertexWithUV(textureXScale * (u2 - u1) / 2, 0, textureYScale * -(v2 - v1) / 2, u2, v1);
        tessellator.addVertexWithUV(textureXScale * -(u2 - u1) / 2, 0, textureYScale * -(v2 - v1) / 2, u1, v1);
        tessellator.addVertexWithUV(textureXScale * -(u2 - u1) / 2, 0, textureYScale * (v2 - v1) / 2, u1, v2);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float p_70070_1_) {
        int i = MathHelper.floor_double(renderPosX);
        int j = MathHelper.floor_double(renderPosZ);

        if (this.worldObj.blockExists(i, 0, j)) {
            double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D;
            int k = MathHelper.floor_double(renderPosY - (double) this.yOffset + d0);
            return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
        } else {
            return 0;
        }
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

        return (int) (((int) rr << 16) + ((int) rg << 8) + (rb));
    }

    public int getFXLayer() {
        return 0;
    }

    public boolean imageLoaded() {
        if (imageData.imageLoaded()) {
            this.width = width < 0 ? imageData.getTotalWidth() : width;
            this.height = height < 0 ? imageData.getTotalHeight() : height;
            return true;
        }
        return false;
    }
}
