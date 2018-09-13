package noppes.npcs.client.fx;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;



public class EntityRainbowFX extends EntityFX
{

    public static float colorTable[][] = {
        {
            1.0F, 0, 0
        },
        {
            1.0F, 0.5f, 0
        },
        {
            1.0F, 1.0f, 0
        },
        {
            0F, 1.0f, 0
        },
        {
            0F, 0, 1.0f
        },
        {
            0,4375F, 0, 1.0f
        },
        {
            0.5625F, 0, 1.0f
        }
    };
    public EntityRainbowFX(World world, double d, double d1, double d2, 
            double f, double f1, double f2)
    {
        this(world, d, d1, d2, 1.0F, f, f1, f2);
    }

    public EntityRainbowFX(World world, double d, double d1, double d2, 
            float f, double f1, double f2, double f3)
    {
        super(world, d, d1, d2, 0.0D, 0.0D, 0.0D);
        motionX *= 0.10000000149011612D;
        motionY *= 0.10000000149011612D;
        motionZ *= 0.10000000149011612D;
        if(f1 == 0.0F)
        {
            f1 = 1.0F;
        }
        int i = world.rand.nextInt(colorTable.length);
        particleRed = colorTable[i][0];
        particleGreen = colorTable[i][1];
        particleBlue = colorTable[i][2];
        particleScale *= 0.75F;
        particleScale *= f;
        reddustParticleScale = particleScale;
        particleMaxAge = (int)(16D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D));
        particleMaxAge *= f;
        noClip = false;
    }

    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
    {
        float f6 = (((float)particleAge + f) / (float)particleMaxAge) * 32F;
        if(f6 < 0.0F)
        {
            f6 = 0.0F;
        }
        if(f6 > 1.0F)
        {
            f6 = 1.0F;
        }
        particleScale = reddustParticleScale * f6;
        super.renderParticle(tessellator, f, f1, f2, f3, f4, f5);
    }

    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if(particleAge++ >= particleMaxAge)
        {
            setDead();
        }
        setParticleTextureIndex(7 - (particleAge * 8) / particleMaxAge);
        moveEntity(motionX, motionY, motionZ);
        if(posY == prevPosY)
        {
            motionX *= 1.1000000000000001D;
            motionZ *= 1.1000000000000001D;
        }
        motionX *= 0.95999997854232788D;
        motionY *= 0.95999997854232788D;
        motionZ *= 0.95999997854232788D;
        if(onGround)
        {
            motionX *= 0.69999998807907104D;
            motionZ *= 0.69999998807907104D;
        }
    }

    float reddustParticleScale;
}
