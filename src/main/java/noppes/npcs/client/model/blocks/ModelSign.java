package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSign extends ModelBase{
    public ModelRenderer Sign;
    ModelRenderer Chain2;
    ModelRenderer Bar;
    ModelRenderer Chain1;
  
  public ModelSign()
  {
      Sign = new ModelRenderer(this, 0, 22);
      Sign.addBox(0F, 0F, 0F, 14, 9, 1);
      Sign.setRotationPoint(-7F, 12F, -0.5F);
      setRotation(Sign, 0.0174533F, 0F, 0F);
      
      Chain2 = new ModelRenderer(this, 0, 0);
      Chain2.addBox(0F, 0F, 0F, 1, 2, 2);
      Chain2.setRotationPoint(5F, 11F, -1F);
      
      Bar = new ModelRenderer(this, 0, 0);
      Bar.addBox(0F, 0F, 0F, 16, 1, 1);
      Bar.setRotationPoint(-8F, 10F, -0.5F);
      
      Chain1 = new ModelRenderer(this, 0, 0);
      Chain1.addBox(0F, 0F, 0F, 1, 2, 2);
      Chain1.setRotationPoint(-6F, 11F, -1F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Chain2.render(f5);
    Bar.render(f5);
    Chain1.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
