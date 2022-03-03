package noppes.npcs.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientProxy;
import org.lwjgl.opengl.GL11;

public class EntityCustomFX extends EntityFX {
    private Entity entity;
    private final ResourceLocation location;
    private static final ResourceLocation resource = new ResourceLocation("textures/particle/particles.png");
    private boolean move = true;
    private float startX = 0, startY = 0, startZ = 0;

    public float scale1 = 1.0F;
    public float scale2 = 1.0F;
    public float scaleRate = 1.0F;
    public int scaleRateStart = 0;

    public float alpha1 = 1.0F;
    public float alpha2 = 0;
    public float alphaRate = 0.0F;
    public int alphaRateStart = 0;

    public float rotation = 0;
    public float rotation1 = 0;
    public float rotation2 = 0;
    public float rotationRate = 0.0F;
    public int rotationRateStart = 0;

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

	public EntityCustomFX(Entity entity, String directory, int HEXColor, double x, double y, double z,
                          double motionX, double motionY, double motionZ, float gravity,
                          float scale1, float scale2, float scaleRate, int scaleRateStart,
                          float alpha1, float alpha2, float alphaRate, int alphaRateStart,
                          float rotation1, float rotation2, float rotationRate, int rotationRateStart,
                          float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart,
                          float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart,
                          float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart,
                          int age) {
		super(entity.worldObj, x, y, z, motionX, motionY, motionZ);

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

        this.rotation1 = rotation1;
        this.rotation2 = rotation2;
        this.rotationRate = Math.abs(rotationRate);
        this.rotationRateStart = rotationRateStart;
        rotation = rotation1;
        if(rotation1 > rotation2)
            this.rotationRate *= -1;

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

        particleRed = (HEXColor >> 16 & 255) / 255f;
        particleGreen = (HEXColor >> 8  & 255) / 255f;
        particleBlue = (HEXColor & 255) / 255f;
        
        if(Math.floor(Math.random()*3) == 1){
        	move = false;
            this.startX = (float) entity.posX;
            this.startY = (float) entity.posY;
            this.startZ = (float) entity.posZ;
        }

        location = new ResourceLocation(directory);
	}

	@Override
    public void renderParticle(Tessellator tessellator, float partialTick, float cosYaw, float cosPitch, float sinYaw, float sinSinPitch, float cosSinPitch)
    {
        tessellator.draw();
        GL11.glPushMatrix();
            ClientProxy.bindTexture(location);

            if(move){
                startX = (float)(entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTick);
                startY = (float)(entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTick);
                startZ = (float)(entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTick);
            }

            float scaleChange = this.scaleRate / (float)particleMaxAge;
            if((this.scaleRate < 0 && particleScale+scaleChange < this.scale2) || (this.scaleRate > 0 && particleScale+scaleChange > this.scale2))
                particleScale = this.scale2;
            else if(particleAge >= this.scaleRateStart)
                particleScale += scaleChange;

            float alphaChange = this.alphaRate / (float)particleMaxAge;
            if((this.alphaRate < 0 && particleAlpha+alphaChange < this.alpha2) || (this.alphaRate > 0 && particleAlpha+alphaChange > this.alpha2))
                particleAlpha = this.alpha2;
            else if(particleAge >= this.alphaRateStart)
                particleAlpha += alphaChange;

            float rotationChange = this.rotationRate / (float)particleMaxAge;
            if((this.rotationRate < 0 && rotation+rotationChange < this.rotation2) || (this.rotationRate > 0 && rotation+rotationChange > this.rotation2))
                rotation = this.rotation2;
            else if(particleAge >= this.rotationRateStart)
                rotation += rotationChange;

            float rotationXChange = this.rotationXRate / (float)particleMaxAge;
            if((this.rotationXRate < 0 && rotationX+rotationXChange < this.rotationX2) || (this.rotationXRate > 0 && rotationX+rotationXChange > this.rotationX2))
                rotationX = this.rotationX2;
            else if(particleAge >= this.rotationXRateStart)
                rotationX += rotationXChange;

            float rotationYChange = this.rotationYRate / (float)particleMaxAge;
            if((this.rotationYRate < 0 && rotationY+rotationYChange < this.rotationY2) || (this.rotationYRate > 0 && rotationY+rotationYChange > this.rotationY2))
                rotationY = this.rotationY2;
            else if(particleAge >= this.rotationYRateStart)
                rotationY += rotationYChange;

            float rotationZChange = this.rotationZRate / (float)particleMaxAge;
            if((this.rotationZRate < 0 && rotationZ+rotationZChange < this.rotationZ2) || (this.rotationZRate > 0 && rotationZ+rotationZChange > this.rotationZ2))
                rotationZ = this.rotationZ2;
            else if(particleAge >= this.rotationZRateStart)
                rotationZ += rotationZChange;

            float u1 = 0.0f;
            float u2 = 1.0f;
            float v1 = 0.0f;
            float v2 = 1.0f;

            float renderScale = 0.1F * particleScale;

            float posX = (float)(((prevPosX + (this.posX - prevPosX) * (double)partialTick) - interpPosX) + startX);
            float posY = (float)(((prevPosY + (this.posY - prevPosY) * (double)partialTick) - interpPosY) + startY);
            float posZ = (float)(((prevPosZ + (this.posZ - prevPosZ) * (double)partialTick) - interpPosZ) + startZ);

            GL11.glTranslated((double)posX, (double)posY, (double)posZ);

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            double yaw = (player.rotationYaw/180)*Math.PI;
            double pitch = (player.rotationPitch/180)*Math.PI;

            GL11.glRotated(rotation, Math.sin(yaw)*Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw)*Math.cos(pitch));
            GL11.glRotated(rotationX, 1.0,0.0,0.0);
            GL11.glRotated(rotationY, 0.0,1.0,0.0);
            GL11.glRotated(rotationZ, 0.0,0.0,1.0);

            tessellator.startDrawingQuads();
            tessellator.setBrightness(240);
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
            tessellator.addVertexWithUV( - cosYaw * renderScale - sinSinPitch * renderScale,  - cosPitch * renderScale,  - sinYaw * renderScale - cosSinPitch * renderScale,     u2, v2);
            tessellator.addVertexWithUV(( - cosYaw * renderScale) + sinSinPitch * renderScale,  + cosPitch * renderScale, ( - sinYaw * renderScale) + cosSinPitch * renderScale, u2, v1);
            tessellator.addVertexWithUV( + cosYaw * renderScale + sinSinPitch * renderScale,  + cosPitch * renderScale,  + sinYaw * renderScale + cosSinPitch * renderScale,     u1, v1);
            tessellator.addVertexWithUV(( + cosYaw * renderScale) - sinSinPitch * renderScale,  - cosPitch * renderScale, ( + sinYaw * renderScale) - cosSinPitch * renderScale, u1, v2);

            tessellator.draw();
        GL11.glPopMatrix();

        GL11.glColor4f(1, 1, 1, 1.0F);
        ClientProxy.bindTexture(resource);
        tessellator.startDrawingQuads();
    }
    
    public int getFXLayer(){
    	return 0;
    }
}
