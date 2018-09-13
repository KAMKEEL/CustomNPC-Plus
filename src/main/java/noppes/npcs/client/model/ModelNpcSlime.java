package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelNpcSlime extends ModelBase
{
    ModelRenderer outerBody;
    ModelRenderer innerBody;

    /** The slime's right eye */
    ModelRenderer slimeRightEye;

    /** The slime's left eye */
    ModelRenderer slimeLeftEye;

    /** The slime's mouth */
    ModelRenderer slimeMouth;

    public ModelNpcSlime(int par1)
    {
    	this.textureHeight = 64;
    	this.textureWidth = 64;
        outerBody = new ModelRenderer(this, 0, 0);
        this.outerBody = new ModelRenderer(this, 0, 0);
        this.outerBody.addBox(-8.0F, 32.0F, -8.0F, 16, 16, 16);
        //this.outerBody.addBox(-8.0F, 32.0F, -8.0F, 16, 16, 16);

        if (par1 > 0)
        {
            this.innerBody = new ModelRenderer(this, 0, 32);
            this.innerBody.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
            
            this.slimeRightEye = new ModelRenderer(this, 0, 0);
            this.slimeRightEye.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
            this.slimeLeftEye = new ModelRenderer(this, 0, 4);
            this.slimeLeftEye.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
            this.slimeMouth = new ModelRenderer(this, 0, 8);
            this.slimeMouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
        }
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
        if(innerBody != null)
        	this.innerBody.render(par7);
        else{
        	GL11.glPushMatrix();
        	GL11.glScalef(0.5f, 0.5f, 0.5f);
        	this.outerBody.render(par7);
        	GL11.glPopMatrix();
        }

        if (this.slimeRightEye != null)
        {
            this.slimeRightEye.render(par7);
            this.slimeLeftEye.render(par7);
            this.slimeMouth.render(par7);
        }
    }
}
