package noppes.npcs.client.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class RenderNpcSlime extends RenderNPCInterface
{
    private ModelBase scaleAmount;

    public RenderNpcSlime(ModelBase par1ModelBase, ModelBase par2ModelBase, float par3)
    {
        super(par1ModelBase, par3);
        this.scaleAmount = par2ModelBase;
    }

    /**
     * Determines whether Slime Render should pass or not.
     */
    protected int shouldSlimeRenderPass(EntityNPCInterface par1EntitySlime, int par2, float par3)
    {
        if (par1EntitySlime.isInvisible())
        {
            return 0;
        }
        else 
        	if (par2 == 0)
        {
            this.setRenderPassModel(this.scaleAmount);
            GL11.glEnable(GL11.GL_NORMALIZE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            return 1;
        }
        else
        {
            if (par2 == 1)
            {
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            return -1;
        }
    }

//    /**
//     * sets the scale for the slime based on getSlimeSize in EntitySlime
//     */
//    protected void scaleSlime(EntityNPCInterface par1EntitySlime, float par2)
//    {
//        float var3 = (float)par1EntitySlime.getSlimeSize();
//        float var4 = (par1EntitySlime.field_70812_c + (par1EntitySlime.field_70811_b - par1EntitySlime.field_70812_c) * par2) / (var3 * 0.5F + 1.0F);
//        float var5 = 1.0F / (var4 + 1.0F);
//        GL11.glScalef(var5 * var3, 1.0F / var5 * var3, var5 * var3);
//    }
//
//    /**
//     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
//     * entityLiving, partialTickTime
//     */
//    protected void preRenderCallback(EntityLiving par1EntityLiving, float par2)
//    {
//        this.scaleSlime((EntityNPCInterface)par1EntityLiving, par2);
//    }

    /**
     * Queries whether should render the specified pass or not.
     */
    @Override
    protected int shouldRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3)
    {
        return this.shouldSlimeRenderPass((EntityNPCInterface)par1EntityLiving, par2, par3);
    }
}
