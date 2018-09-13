package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBarrelLit extends ModelBase
{
	ModelRenderer Top;
    ModelRenderer Bottom;
  
  public ModelBarrelLit()
  {
      Top = new ModelRenderer(this, 0, 0);
      Top.addBox(0F, 0F, 0F, 16, 0, 16);
      Top.setRotationPoint(-8F, 9F, -8F);
      
      Bottom = new ModelRenderer(this, 0, 0);
      Bottom.addBox(0F, 0F, 0F, 16, 0, 16);
      Bottom.setRotationPoint(-8F, 23F, -8F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Bottom.render(f5);
    Top.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = y;
    model.rotateAngleY = x;
    model.rotateAngleZ = z;
  }
  

}
