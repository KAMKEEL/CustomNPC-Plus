package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelChair extends ModelBase
{
  //fields
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
  
  public ModelChair()
  {
      Leg1 = new ModelRenderer(this, 0, 0);
      Leg1.mirror = true;
      Leg1.addBox(0F, 0F, 0F, 1, 18, 1);
      Leg1.setRotationPoint(4.01F, 6F, 5.01F);
      
      Leg2 = new ModelRenderer(this, 0, 0);
      Leg2.mirror = true;
      Leg2.addBox(0F, 0F, 0F, 1, 9, 1);
      Leg2.setRotationPoint(4.01F, 15.5F, -5.01F);
      
      Leg3 = new ModelRenderer(this, 0, 0);
      Leg3.addBox(0F, 0F, 0F, 1, 18, 1);
      Leg3.setRotationPoint(-5.01F, 6F, 5.01F);
      
      Leg4 = new ModelRenderer(this, 0, 0);
      Leg4.addBox(0F, 0F, 0F, 1, 9, 1);
      Leg4.setRotationPoint(-5.01F, 15.5F, -5.01F);
      
      Shape1 = new ModelRenderer(this, 8, 2);
      Shape1.addBox(0F, 0F, 0F, 10, 1, 11);
      Shape1.setRotationPoint(-5F, 16F, -5F);
      
      Shape2 = new ModelRenderer(this, 4, 4);
      Shape2.addBox(0F, 0F, 0F, 3, 2, 1);
      Shape2.setRotationPoint(-1.5F, 6.51F, 5.5F);
      
      Shape3 = new ModelRenderer(this, 4, 4);
      Shape3.mirror = true;
      Shape3.addBox(-3F, 0F, 0F, 3, 2, 1);
      Shape3.setRotationPoint(4F, 6.5F, 5F);
      setRotation(Shape3, 0F, 0.2094395F, 0F);
      
      Shape4 = new ModelRenderer(this, 4, 4);
      Shape4.addBox(0F, 0F, 0F, 3, 2, 1);
      Shape4.setRotationPoint(-4F, 6.5F, 5F);
      setRotation(Shape4, 0F, -0.2094395F, 0F);
      
      Shape5 = new ModelRenderer(this, 46, 0);
      Shape5.addBox(0F, 0F, 0F, 9, 1, 1);
      Shape5.setRotationPoint(-4F, 19F, 5F);
      
      Shape6 = new ModelRenderer(this, 46, 0);
      Shape6.addBox(0F, 0F, 0F, 8, 1, 1);
      Shape6.setRotationPoint(-4F, 19F, -5F);
      
      Shape7 = new ModelRenderer(this, 11, 13);
      Shape7.addBox(0F, 0F, 0F, 1, 1, 9);
      Shape7.setRotationPoint(-5F, 20F, -4F);
      
      Shape8 = new ModelRenderer(this, 11, 13);
      Shape8.mirror = true;
      Shape8.addBox(0F, 0F, 0F, 1, 1, 9);
      Shape8.setRotationPoint(4F, 20F, -4F);
      
      Shape9 = new ModelRenderer(this, 0, 0);
      Shape9.mirror = true;
      Shape9.addBox(0F, 0F, 0F, 1, 8, 1);
      Shape9.setRotationPoint(2F, 8F, 5.5F);
      setRotation(Shape9, -0.0523599F, 0F, 0F);
      
      Shape10 = new ModelRenderer(this, 0, 0);
      Shape10.addBox(0F, 0F, 0F, 1, 8, 1);
      Shape10.setRotationPoint(-3F, 8F, 5.5F);
      setRotation(Shape10, -0.0523599F, 0F, 0F);
      
      Shape11 = new ModelRenderer(this, 0, 0);
      Shape11.addBox(0F, 0F, 0F, 1, 8, 1);
      Shape11.setRotationPoint(-0.5F, 8F, 5.6F);
      setRotation(Shape11, -0.0698132F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    Leg1.render(f5);
    Leg2.render(f5);
    Leg3.render(f5);
    Leg4.render(f5);
    Shape1.render(f5);
    Shape2.render(f5);
    Shape3.render(f5);
    Shape4.render(f5);
    Shape5.render(f5);
    Shape6.render(f5);
    Shape7.render(f5);
    Shape8.render(f5);
    Shape9.render(f5);
    Shape10.render(f5);
    Shape11.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

}
