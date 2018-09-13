package noppes.npcs.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.entity.EntityCustomNpc;

import org.lwjgl.opengl.GL11;

public class EntityEnderFX extends EntityPortalFX{

    private float portalParticleScale;
    private int particleNumber;
    private EntityCustomNpc npc;
    private static final ResourceLocation resource = new ResourceLocation("textures/particle/particles.png");
    private final ResourceLocation location;
    private boolean move = true;
    private float startX = 0, startY = 0, startZ = 0;
    
	public EntityEnderFX(EntityCustomNpc npc, double par2, double par4,
			double par6, double par8, double par10, double par12, ModelPartData data) {
		super(npc.worldObj, par2, par4, par6, par8, par10, par12);
		
		this.npc = npc;
		particleNumber = npc.getRNG().nextInt(2);
        portalParticleScale = particleScale = rand.nextFloat() * 0.2F + 0.5F;

        particleRed = (data.color >> 16 & 255) / 255f;
        particleGreen = (data.color >> 8  & 255) / 255f;
        particleBlue = (data.color & 255) / 255f;
        
        if(npc.getRNG().nextInt(3) == 1){
        	move = false;
            this.startX = (float) npc.posX;
            this.startY = (float) npc.posY;
            this.startZ = (float) npc.posZ;
        }
        
        if(data.playerTexture)
        	location = npc.textureLocation;
        else
        	location = new ResourceLocation(data.texture);
	}

	@Override
    public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {		
		if(move){
			startX = (float)(npc.prevPosX + (npc.posX - npc.prevPosX) * (double)par2);
			startY = (float)(npc.prevPosY + (npc.posY - npc.prevPosY) * (double)par2);
			startZ = (float)(npc.prevPosZ + (npc.posZ - npc.prevPosZ) * (double)par2);
		}
        Tessellator tessellator = Tessellator.instance;
        tessellator.draw();
        float scale = ((float)particleAge + par2) / (float)particleMaxAge;
        scale = 1.0F - scale;
        scale *= scale;
        scale = 1.0F - scale;
        particleScale = portalParticleScale * scale;
        
    	ClientProxy.bindTexture(location);
    	
        float f = 0.875f;
        float f1 = f + 0.125f;
        float f2 = 0.75f - (particleNumber * 0.25f);
        float f3 = f2 + 0.25f;
        float f4 = 0.1F * particleScale;
        float f5 = (float)(((prevPosX + (posX - prevPosX) * (double)par2) - interpPosX) + startX);
        float f6 = (float)(((prevPosY + (posY - prevPosY) * (double)par2) - interpPosY) + startY);
        float f7 = (float)(((prevPosZ + (posZ - prevPosZ) * (double)par2) - interpPosZ) + startZ);

        GL11.glColor4f(1, 1, 1, 1.0F);
        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        par1Tessellator.setColorOpaque_F(1, 1, 1);
        par1Tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, 1);
        par1Tessellator.addVertexWithUV(f5 - par3 * f4 - par6 * f4, f6 - par4 * f4, f7 - par5 * f4 - par7 * f4, f1, f3);
        par1Tessellator.addVertexWithUV((f5 - par3 * f4) + par6 * f4, f6 + par4 * f4, (f7 - par5 * f4) + par7 * f4, f1, f2);
        par1Tessellator.addVertexWithUV(f5 + par3 * f4 + par6 * f4, f6 + par4 * f4, f7 + par5 * f4 + par7 * f4, f, f2);
        par1Tessellator.addVertexWithUV((f5 + par3 * f4) - par6 * f4, f6 - par4 * f4, (f7 + par5 * f4) - par7 * f4, f, f3);
        
        tessellator.draw();
        ClientProxy.bindTexture(resource);
        tessellator.startDrawingQuads();
    }
    
    public int getFXLayer(){
    	return 0;
    }
}
