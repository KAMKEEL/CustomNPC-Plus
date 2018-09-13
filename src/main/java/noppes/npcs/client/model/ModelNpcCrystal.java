package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNpcCrystal;

import org.lwjgl.opengl.GL11;

public class ModelNpcCrystal extends ModelBase
{
    private ModelRenderer field_41057_g;
    private ModelRenderer field_41058_h;
    private ModelRenderer field_41059_i;

    public ModelNpcCrystal(float par1)
    {
        field_41058_h = new ModelRenderer(this, "glass");
        field_41058_h.setTextureOffset(0, 0).addBox(-4F, -4F, -4F, 8, 8, 8);
        field_41057_g = new ModelRenderer(this, "cube");
        field_41057_g.setTextureOffset(32, 0).addBox(-4F, -4F, -4F, 8, 8, 8);
        field_41059_i = new ModelRenderer(this, "base");
        field_41059_i.setTextureOffset(0, 16).addBox(-6F, 16.0F, -6F, 12, 4, 12);
    }
    float ticks;
    @Override
    public void setLivingAnimations(EntityLivingBase par1EntityLiving, float f6, float f5, float par9){
    	ticks = par9;
    }
    /**
     * Sets the models various rotation angles then renders the model.
     */
    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
    	
        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glTranslatef(0.0F, -0.5F, 0.0F);
        field_41059_i.render(par7);

        float f = (float)par1Entity.ticksExisted + ticks;
        float f1 = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = f1 * f1 + f1;
        
        par3 = f * 3F;
        par4 = f1 * 0.2F;
        
        GL11.glRotatef(par3, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, 0.1F + par4, 0.0F);
        GL11.glRotatef(60F, 0.7071F, 0.0F, 0.7071F);
        field_41058_h.render(par7);
        float sca = 0.875F;
        GL11.glScalef(sca, sca, sca);
        GL11.glRotatef(60F, 0.7071F, 0.0F, 0.7071F);
        GL11.glRotatef(par3, 0.0F, 1.0F, 0.0F);
        field_41058_h.render(par7);
        GL11.glScalef(sca, sca, sca);
        GL11.glRotatef(60F, 0.7071F, 0.0F, 0.7071F);
        GL11.glRotatef(par3, 0.0F, 1.0F, 0.0F);
        field_41057_g.render(par7);
        GL11.glPopMatrix();
    }

}
