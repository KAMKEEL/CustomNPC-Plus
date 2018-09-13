package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBarrel extends ModelBase
{
    ModelRenderer Plank1;
    ModelRenderer Plank2;
    ModelRenderer Plank3;
    ModelRenderer Plank4;
    ModelRenderer Plank5;
    ModelRenderer Plank6;
    ModelRenderer Plank7;
    ModelRenderer Plank8;
    ModelRenderer Plank9;
    ModelRenderer Plank10;
    ModelRenderer Plank11;
    ModelRenderer Plank12;
  
  public ModelBarrel()
  {      
      Plank1 = new ModelRenderer(this, 10, 0);
      Plank1.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank1.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank1, 0F, 0F, 1.570796F);
      
      Plank2 = new ModelRenderer(this, 10, 8);
      Plank2.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank2.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank2, 0F, 0.5235988F, 1.570796F);
      
      Plank3 = new ModelRenderer(this, 10, 0);
      Plank3.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank3.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank3, 0F, 1.047198F, 1.570796F);
      
      Plank4 = new ModelRenderer(this, 10, 8);
      Plank4.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank4.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank4, 0F, 1.570796F, 1.570796F);
      
      Plank5 = new ModelRenderer(this, 10, 0);
      Plank5.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank5.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank5, 0F, 2.094395F, 1.570796F);
      
      Plank6 = new ModelRenderer(this, 10, 8);
      Plank6.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank6.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank6, 0F, 2.617994F, 1.570796F);
      
      Plank7 = new ModelRenderer(this, 10, 0);
      Plank7.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank7.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank7, 0F, 3.150901F, 1.570796F);
      
      Plank8 = new ModelRenderer(this, 10, 8);
      Plank8.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank8.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank8, 0F, -2.617994F, 1.570796F);
      
      Plank9 = new ModelRenderer(this, 10, 0);
      Plank9.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank9.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank9, 0F, -2.094395F, 1.570796F);
      
      Plank10 = new ModelRenderer(this, 10, 8);
      Plank10.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank10.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank10, 0F, -1.570796F, 1.570796F);
      
      Plank11 = new ModelRenderer(this, 10, 0);
      Plank11.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank11.setRotationPoint(0F, 7.01F, 0F);
      setRotation(Plank11, 0F, -1.047198F, 1.570796F);
      
      Plank12 = new ModelRenderer(this, 10, 0);
      Plank12.addBox(0F, 6.5F, -2F, 17, 1, 4);
      Plank12.setRotationPoint(0F, 7F, 0F);
      setRotation(Plank12, 0F, -0.5235988F, 1.570796F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Plank1.render(f5);
    Plank2.render(f5);
    Plank3.render(f5);
    Plank4.render(f5);
    Plank5.render(f5);
    Plank6.render(f5);
    Plank7.render(f5);
    Plank8.render(f5);
    Plank9.render(f5);
    Plank10.render(f5);
    Plank11.render(f5);
    Plank12.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = y;
    model.rotateAngleY = x;
    model.rotateAngleZ = z;
  }
  

}
