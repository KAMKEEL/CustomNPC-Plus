package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class ModelTallLamp extends ModelBase
{
  //fields
    ModelRenderer Base;
    ModelRenderer MiddleStick;
    ModelRenderer LampShadeStick1;
    ModelRenderer LampShadeStick2;
    ModelRenderer LampShadeStick3;
    ModelRenderer LampShadeStick4;
  
  public ModelTallLamp()
  {
      Base = new ModelRenderer(this, 6, 2);
      Base.addBox(-6F, 0F, -6F, 12, 1, 12);
      Base.setRotationPoint(0F, 23F, 0F);
      
      MiddleStick = new ModelRenderer(this, 12, 2);
      MiddleStick.addBox(-1F, 0F, -1F, 2, 28, 2);
      MiddleStick.setRotationPoint(0F, -5F, 0F);
      
      LampShadeStick1 = new ModelRenderer(this, 0, 30);
      LampShadeStick1.addBox(0F, 0F, 0F, 5, 1, 1);
      LampShadeStick1.setRotationPoint(1F, -1F, -0.5F);
      
      LampShadeStick2 = new ModelRenderer(this, 0, 30);
      LampShadeStick2.addBox(0F, 0F, 0F, 5, 1, 1);
      LampShadeStick2.setRotationPoint(-0.5F, -1F, -1F);
      setRotation(LampShadeStick2, 0F, 1.570796F, 0F);
      
      LampShadeStick3 = new ModelRenderer(this, 0, 30);
      LampShadeStick3.addBox(0F, 0F, 0F, 5, 1, 1);
      LampShadeStick3.setRotationPoint(-1F, -1F, 0.5F);
      setRotation(LampShadeStick3, 0F, 3.141593F, 0F);
      
      LampShadeStick4 = new ModelRenderer(this, 0, 30);
      LampShadeStick4.addBox(0F, 0F, 0F, 5, 1, 1);
      LampShadeStick4.setRotationPoint(0.5F, -1F, 1F);
      setRotation(LampShadeStick4, 0F, -1.570796F, 0F);
  }
  
  @Override
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Base.render(f5);
    MiddleStick.render(f5);
    LampShadeStick1.render(f5);
    LampShadeStick2.render(f5);
    LampShadeStick3.render(f5);
    LampShadeStick4.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
