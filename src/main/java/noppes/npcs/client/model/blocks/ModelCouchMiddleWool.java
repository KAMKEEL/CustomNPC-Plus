package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchMiddleWool extends ModelBase
{
    ModelRenderer Wool1;
    ModelRenderer Wool2;
  
  public ModelCouchMiddleWool(){      
      Wool1 = new ModelRenderer(this, 3, 0);
      Wool1.addBox(0F, 0F, 0F, 16, 5, 13);
      Wool1.setRotationPoint(-8F, 16F, -6F);
      
      Wool2 = new ModelRenderer(this, 14, 0);
      Wool2.addBox(0F, 0F, 0F, 16, 10, 2);
      Wool2.setRotationPoint(-8F, 6F, 5F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Wool1.render(f5);
    Wool2.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
