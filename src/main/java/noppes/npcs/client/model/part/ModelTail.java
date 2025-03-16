package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.part.tails.*;
import noppes.npcs.client.model.util.ModelScaleRenderer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.controllers.data.TintData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.ModelPartData;
import org.lwjgl.opengl.GL11;

public class ModelTail extends ModelScaleRenderer {
    private EntityCustomNpc entity;
    private ModelMPM base;

    public ModelRenderer tail;
    public ModelRenderer dragon;
    public ModelRenderer squirrel;
    public ModelRenderer horse;
    public ModelRenderer fin;
    public ModelRenderer rodent;
    public ModelRenderer feather;
    public ModelCanineTail fox;
    public ModelMonkeyTail monkey;

    private int color = 0xFFFFFF;

    private ResourceLocation location = null;

    public ModelTail(ModelMPM base) {
        super(base);
        this.base = base;
        this.rotationPointY = 11;

        tail = new ModelRenderer(base, 56, 21);
        tail.setTextureSize(64, 32);
        tail.addBox(-1F, 0F, 0F, 2, 9, 2);
        tail.setRotationPoint(0F, 0, 1F);
        setRotation(tail, 0.8714253F, 0F, 0F);
        this.addChild(tail);

        horse = new ModelRenderer(base);
        horse.setTextureSize(32, 32);
        horse.setRotationPoint(0, -1, 1);
        this.addChild(horse);

        ModelRenderer tailBase = new ModelRenderer(base, 0, 26);
        tailBase.setTextureSize(32, 32);
        tailBase.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 3);
        setRotation(tailBase, -1.134464F, 0.0F, 0.0F);
        horse.addChild(tailBase);
        ModelRenderer tailMiddle = new ModelRenderer(base, 0, 13);
        tailMiddle.setTextureSize(32, 32);
        tailMiddle.addBox(-1.5F, -2.0F, 3.0F, 3, 4, 7);
        setRotation(tailMiddle, -1.134464F, 0.0F, 0.0F);
        horse.addChild(tailMiddle);
        ModelRenderer tailTip = new ModelRenderer(base, 0, 0);
        tailTip.setTextureSize(32, 32);
        tailTip.addBox(-1.5F, -4.5F, 9.0F, 3, 4, 7);
        setRotation(tailTip, -1.40215F, 0.0F, 0.0F);
        horse.addChild(tailTip);
        horse.rotateAngleX = 0.5f;

        this.addChild(dragon = new ModelDragonTail(base));
        this.addChild(squirrel = new ModelSquirrelTail(base));
        this.addChild(fin = new ModelTailFin(base));
        this.addChild(rodent = new ModelRodentTail(base));
        this.addChild(feather = new ModelFeatherTail(base));
        this.addChild(fox = new ModelCanineTail(base));
        this.addChild(monkey = new ModelMonkeyTail(base));
    }

    public void setData(EntityCustomNpc entity) {
        this.entity = entity;
        initData(entity);
    }

    public void setRotationAngles(float par1, float par2, float par3,
                                  float par4, float par5, float par6, Entity entity) {
        if (this.entity == null) return;

        float rotateAngleY = MathHelper.cos(par1 * 0.6662F) * 0.3f * par2;
        float rotateAngleX = MathHelper.sin(par3 * 0.067F) * 0.05F;

        rotationPointY = 11;
        if (this.entity.modelData.legParts.type == 2) {
            rotationPointY = 13f;
            rotationPointZ = 14 * this.entity.modelData.modelScale.legs.scaleZ;

            if (base.isSleeping(entity) || this.entity.currentAnimation == EnumAnimation.CRAWLING) {
                rotationPointY = 12 + 16 * this.entity.modelData.modelScale.legs.scaleZ;
                rotationPointZ = this.entity.modelData.modelScale.legs.scaleY;

                rotateAngleX = (float) (Math.PI / -4);
            }
        } else if (this.entity.modelData.legParts.type == 3) {
            // rotationPointY = 8.6f;
            rotationPointY = 10;
            rotationPointZ = 19 * this.entity.modelData.modelScale.legs.scaleZ;
        } else {
            // this.rotationPointZ = -1;
            rotationPointZ = -1;
        }
        ModelPartData partTail = this.entity.modelData.getPartData("tail");
        if (partTail != null) {
            if (partTail.type == 2) {
                rotateAngleX += 0.5F;
            }
            if (partTail.type == 0) {
                rotateAngleX += 0.87F;
            }
            if (partTail.type == 7) {
                rotationPointY -= 1;
                fox.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
            }
            if (partTail.type == 8) {
                monkey.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
                if (partTail.pattern == 1) {
                    rotationPointZ += 1;
                    rotateAngleX = 0;
                }
            }
        }
        this.rotationPointZ += base.bipedRightLeg.rotationPointZ + 0.5f;
        monkey.rotateAngleX = fox.rotateAngleX = tail.rotateAngleX = feather.rotateAngleX = dragon.rotateAngleX = squirrel.rotateAngleX = horse.rotateAngleX = fin.rotateAngleX = rodent.rotateAngleX = rotateAngleX;
        monkey.rotateAngleY = fox.rotateAngleY = tail.rotateAngleY = feather.rotateAngleY = dragon.rotateAngleY = squirrel.rotateAngleY = horse.rotateAngleY = fin.rotateAngleY = rodent.rotateAngleY = rotateAngleY;
    }

    public void setLivingAnimations(ModelPartData data, EntityLivingBase entity, float par2, float par3, float par4) {
    }

    public void initData(EntityCustomNpc data) {
        ModelPartData config = data.modelData.getPartData("tail");
        if (config == null) {
            isHidden = true;
            return;
        }
        color = config.color;
        isHidden = false;
        tail.isHidden = config.type != 0;
        dragon.isHidden = config.type != 1;
        horse.isHidden = config.type != 2;
        squirrel.isHidden = config.type != 3;
        fin.isHidden = config.type != 4;
        rodent.isHidden = config.type != 5;
        feather.isHidden = config.type != 6;
        fox.isHidden = config.type != 7;

        monkey.isHidden = config.type != 8;
        monkey.monkey.isHidden = config.pattern != 0;
        monkey.monkey_wrapped.isHidden = config.pattern != 1;
        monkey.monkey_large.isHidden = config.pattern != 2;

        if (!config.playerTexture) {
            location = config.getResource();
        } else
            location = null;
    }

    @Override
    public void render(float par1) {
        if (isHidden)
            return;
        if (!base.isArmor) {
            if (location != null) {
                ClientProxy.bindTexture(location);
                base.currentlyPlayerTexture = false;
            } else if (!base.currentlyPlayerTexture) {
                ClientProxy.bindTexture(entity.textureLocation);
                base.currentlyPlayerTexture = true;
            }
        }
        TintData tintData = entity.display.tintData;
        boolean showColor = !base.isArmor && tintData.processColor(entity.hurtTime > 0 || entity.deathTime > 0);
        if (showColor) {
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8 & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red, green, blue, base.alpha);
        }
        super.render(par1);
        if (showColor) {
            GL11.glColor4f(1, 1, 1, base.alpha);
        }
    }

}
