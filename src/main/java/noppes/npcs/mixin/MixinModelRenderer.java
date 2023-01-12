package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.AnimationData;
import noppes.npcs.AnimationDataShared;
import noppes.npcs.AnimationPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.client.renderer.RenderCustomNpc;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = ModelRenderer.class)
public abstract class MixinModelRenderer {
    @Shadow public float rotationPointX;
    @Shadow public float rotationPointY;
    @Shadow public float rotationPointZ;
    @Shadow public float rotateAngleX;
    @Shadow public float rotateAngleY;
    @Shadow public float rotateAngleZ;
    @Shadow public float offsetX;
    @Shadow public float offsetY;
    @Shadow public float offsetZ;
    @Shadow public boolean isHidden;
    @Shadow public boolean showModel;
    @Shadow public boolean compiled;
    @Shadow private int displayList;
    @Shadow public List childModels;
    @Shadow abstract void compileDisplayList(float p_78788_1_);

    HashMap<AnimationPartConfig, String> modelNameMap = new HashMap<>();
    String partName = "";
    HashMap<String,String[]> partNames = new HashMap<>();

    /**
     * @author Someone
     * @reason Because we need it
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void render(float p_78785_1_)
    {
        AnimationPartConfig currentPart = null;

        if (partNames.isEmpty()) {
            String[] headNames = new String[]{"field_78116_c","bipedHead","bipedHeadwear","head","Head","bipedHeadAll",
                    "bipedHeadg","bipedHeadt","bipedHeadgh","bipedHeadv","bipedHeadb","bipedHeadt2"};
            String[] bodyNames = new String[]{"field_78115_e","bipedBody","B1","body","UpperBody","Body1","BodyBase"};
            String[] larmNames = new String[]{"field_78113_g","bipedLeftArm","LA","leftarm","ArmL","Arm1L","ArmL1"};
            String[] rarmNames = new String[]{"field_78112_f","bipedRightArm","RA","rightarm","ArmR","Arm1R","ArmR1"};
            String[] llegNames = new String[]{"field_78124_i","bipedLeftLeg","LL","leftleg","LegL","Leg1L","LegL1"};
            String[] rlegNames = new String[]{"field_78123_h","bipedRightLeg","RL","rightleg","LegR","Leg1R","LegR1"};

            partNames.put("head", headNames);
            partNames.put("body", bodyNames);
            partNames.put("larm", larmNames);
            partNames.put("rarm", rarmNames);
            partNames.put("lleg", llegNames);
            partNames.put("rleg", rlegNames);
        }
        if (ClientEventHandler.renderingPlayer != null && Client.playerModelData.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
            AnimationDataShared modelData = Client.playerModelData.get(ClientEventHandler.renderingPlayer.getUniqueID());
            AnimationPartConfig[] partConfigs = new AnimationPartConfig[]{modelData.head, modelData.body, modelData.larm, modelData.rarm, modelData.lleg, modelData.rleg};
            this.partName = this.getPartName((ModelRenderer) (Object) this, this.partNames);
            this.setModelParts(modelData);
            if (modelData.enabled()) {
                for (AnimationPartConfig modelPart : partConfigs) {
                    if (this.isPart(modelNameMap, modelPart, this.partName)) {
                        this.rotateAngleX = modelPart.prevRotations[0];
                        this.rotateAngleY = modelPart.prevRotations[1];
                        this.rotateAngleZ = modelPart.prevRotations[2];
                        this.addInterpolatedOffset(modelPart);
                        this.addInterpolatedAngles(modelPart);
                        modelPart.prevRotations = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
                        break;
                    }
                }
            }
        } else if (ClientEventHandler.renderingNpc != null) {
            AnimationData modelData = ClientEventHandler.renderingNpc.display.animationData;
            AnimationPartConfig[] partConfigs = new AnimationPartConfig[]{modelData.head, modelData.body, modelData.larm, modelData.rarm, modelData.lleg, modelData.rleg};
            this.partName = this.getPartName((ModelRenderer) (Object) this, this.partNames);
            this.setModelParts(modelData);
            if (RenderCustomNpc.entity != null) {
                if (modelData.isActive()) {
                    for (AnimationPartConfig modelPart : partConfigs) {
                        if (this.isPart(modelNameMap,modelPart,this.partName)) {
                            currentPart = modelPart;
                            this.addInterpolatedOffset(modelPart);
                            this.addInterpolatedAngles(modelPart);
                            break;
                        }
                    }
                }
            }
        }

        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78785_1_);
                }

                if (currentPart != null) {
                    this.rotationPointX += currentPart.prevPivots[0];
                    this.rotationPointY += currentPart.prevPivots[1];
                    this.rotationPointZ += currentPart.prevPivots[2];
                    this.rotateAngleX += currentPart.prevRotations[0];
                    this.rotateAngleY += currentPart.prevRotations[1];
                    this.rotateAngleZ += currentPart.prevRotations[2];
                }
                GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
                int i;

                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F)
                    {
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }
                    }
                    else
                    {
                        GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }

                        GL11.glTranslatef(-this.rotationPointX * p_78785_1_, -this.rotationPointY * p_78785_1_, -this.rotationPointZ * p_78785_1_);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);

                    if (this.rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }

                    GL11.glCallList(this.displayList);

                    if (this.childModels != null)
                    {
                        for (i = 0; i < this.childModels.size(); ++i)
                        {
                            ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                        }
                    }

                    GL11.glPopMatrix();
                }

                if (currentPart != null) {
                    this.rotationPointX -= currentPart.prevPivots[0];
                    this.rotationPointY -= currentPart.prevPivots[1];
                    this.rotationPointZ -= currentPart.prevPivots[2];
                    this.rotateAngleX -= currentPart.prevRotations[0];
                    this.rotateAngleY -= currentPart.prevRotations[1];
                    this.rotateAngleZ -= currentPart.prevRotations[2];
                }
            }
        }
    }





    public void addInterpolatedAngles(AnimationPartConfig modelPart) {
        if (modelPart == null) {
            return;
        }

        float pi = (float) Math.PI * (modelPart.fullAngles ? 2 : 1);
        if (ClientEventHandler.renderingNpc != null) {
            if (!modelPart.animate) {
                modelPart.prevRotations[0] = modelPart.rotationX * pi;
                modelPart.prevRotations[1] = modelPart.rotationY * pi;
                modelPart.prevRotations[2] = modelPart.rotationZ * pi;
            } else if (modelPart.partialRotationTick != ClientEventHandler.partialRenderTick) {
                modelPart.partialRotationTick = ClientEventHandler.partialRenderTick;
                if (modelPart.interpolate) {
                    modelPart.prevRotations[0] = (modelPart.rotationX * pi - modelPart.prevRotations[0]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevRotations[0];
                    modelPart.prevRotations[1] = (modelPart.rotationY * pi - modelPart.prevRotations[1]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevRotations[1];
                    modelPart.prevRotations[2] = (modelPart.rotationZ * pi - modelPart.prevRotations[2]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevRotations[2];
                } else {
                    int directionX = Float.compare(modelPart.rotationX * pi, modelPart.prevRotations[0]);
                    modelPart.prevRotations[0] += directionX * modelPart.animRate / 10f;
                    modelPart.prevRotations[0] = directionX == 1 ?
                            Math.min(modelPart.rotationX * pi, modelPart.prevRotations[0]) : Math.max(modelPart.rotationX * pi, modelPart.prevRotations[0]);
                    int directionY = Float.compare(modelPart.rotationY * pi, modelPart.prevRotations[1]);
                    modelPart.prevRotations[1] += directionY * modelPart.animRate / 10f;
                    modelPart.prevRotations[1] = directionY == 1 ?
                            Math.min(modelPart.rotationY * pi, modelPart.prevRotations[1]) : Math.max(modelPart.rotationY * pi, modelPart.prevRotations[1]);
                    int directionZ = Float.compare(modelPart.rotationZ * pi, modelPart.prevRotations[2]);
                    modelPart.prevRotations[2] += directionZ * modelPart.animRate / 10f;
                    modelPart.prevRotations[2] = directionZ == 1 ?
                            Math.min(modelPart.rotationZ * pi, modelPart.prevRotations[2]) : Math.max(modelPart.rotationZ * pi, modelPart.prevRotations[2]);
                }
            }
        } else {
            if (!modelPart.animate) {
                this.rotateAngleX = modelPart.rotationX * pi;
                this.rotateAngleY = modelPart.rotationY * pi;
                this.rotateAngleZ = modelPart.rotationZ * pi;
            } else if (modelPart.partialRotationTick != ClientEventHandler.partialRenderTick) {
                modelPart.partialRotationTick = ClientEventHandler.partialRenderTick;
                if (modelPart.interpolate) {
                    if (modelPart.rotationX * pi - this.rotateAngleX != 0)
                        this.rotateAngleX = (modelPart.rotationX * pi - this.rotateAngleX) * Math.abs(modelPart.animRate) / 10f + this.rotateAngleX;
                    if (modelPart.rotationY * pi - this.rotateAngleY != 0)
                        this.rotateAngleY = (modelPart.rotationY * pi - this.rotateAngleY) * Math.abs(modelPart.animRate) / 10f + this.rotateAngleY;
                    if (modelPart.rotationZ * pi - this.rotateAngleZ != 0)
                        this.rotateAngleZ = (modelPart.rotationZ * pi - this.rotateAngleZ) * Math.abs(modelPart.animRate) / 10f + this.rotateAngleZ;
                } else {
                    int directionX = Float.compare(modelPart.rotationX * pi, this.rotateAngleX);
                    this.rotateAngleX += directionX * Math.abs(modelPart.animRate) / 10f;
                    this.rotateAngleX = directionX == 1 ?
                            Math.min(modelPart.rotationX * pi, this.rotateAngleX) : Math.max(modelPart.rotationX * pi, this.rotateAngleX);
                    int directionY = Float.compare(modelPart.rotationY * pi, this.rotateAngleY);
                    this.rotateAngleY += directionY * Math.abs(modelPart.animRate) / 10f;
                    this.rotateAngleY = directionY == 1 ?
                            Math.min(modelPart.rotationY * pi, this.rotateAngleY) : Math.max(modelPart.rotationY * pi, this.rotateAngleY);
                    int directionZ = Float.compare(modelPart.rotationZ * pi, this.rotateAngleZ);
                    this.rotateAngleZ += directionZ * Math.abs(modelPart.animRate) / 10f;
                    this.rotateAngleZ = directionZ == 1 ?
                            Math.min(modelPart.rotationZ * pi, this.rotateAngleZ) : Math.max(modelPart.rotationZ * pi, this.rotateAngleZ);
                }
            }
        }
    }

    public void addInterpolatedOffset(AnimationPartConfig modelPart) {
        if (modelPart == null) {
            return;
        }

        if (!modelPart.animate) {
            modelPart.prevPivots[0] = modelPart.pivotX;
            modelPart.prevPivots[1] = modelPart.pivotY;
            modelPart.prevPivots[2] = modelPart.pivotZ;
        } else if (modelPart.partialPivotTick != ClientEventHandler.partialRenderTick)  {
            modelPart.partialPivotTick = ClientEventHandler.partialRenderTick;
            if (modelPart.interpolate) {
                modelPart.prevPivots[0] = (modelPart.pivotX - modelPart.prevPivots[0]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevPivots[0];
                modelPart.prevPivots[1] = (modelPart.pivotY - modelPart.prevPivots[1]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevPivots[1];
                modelPart.prevPivots[2] = (modelPart.pivotZ - modelPart.prevPivots[2]) * Math.abs(modelPart.animRate) / 10f + modelPart.prevPivots[2];
            } else {
                int directionX = Float.compare(modelPart.pivotX, modelPart.prevPivots[0]);
                modelPart.prevPivots[0] += directionX * modelPart.animRate / 10f;
                modelPart.prevPivots[0] = directionX == 1 ?
                        Math.min(modelPart.pivotX,modelPart.prevPivots[0]) : Math.max(modelPart.pivotX,modelPart.prevPivots[0]);
                int directionY = Float.compare(modelPart.pivotY, modelPart.prevPivots[1]);
                modelPart.prevPivots[1] += directionY * modelPart.animRate / 10f;
                modelPart.prevPivots[1] = directionY == 1 ?
                        Math.min(modelPart.pivotY,modelPart.prevPivots[1]) : Math.max(modelPart.pivotY,modelPart.prevPivots[1]);
                int directionZ = Float.compare(modelPart.pivotZ, modelPart.prevPivots[2]);
                modelPart.prevPivots[2] += directionZ * modelPart.animRate / 10f;
                modelPart.prevPivots[2] = directionZ == 1 ?
                        Math.min(modelPart.pivotZ,modelPart.prevPivots[2]) : Math.max(modelPart.pivotZ,modelPart.prevPivots[2]);
            }
        }
    }

    public void setModelParts(Object modelData) {
        if (modelData instanceof AnimationDataShared) {
            modelNameMap.put(((AnimationDataShared) modelData).head, "head");
            modelNameMap.put(((AnimationDataShared) modelData).body, "body");
            modelNameMap.put(((AnimationDataShared) modelData).rarm, "rarm");
            modelNameMap.put(((AnimationDataShared) modelData).larm, "larm");
            modelNameMap.put(((AnimationDataShared) modelData).rleg, "rleg");
            modelNameMap.put(((AnimationDataShared) modelData).lleg, "lleg");
        }
        if (modelData instanceof AnimationData) {
            modelNameMap.put(((AnimationData) modelData).head, "head");
            modelNameMap.put(((AnimationData) modelData).body, "body");
            modelNameMap.put(((AnimationData) modelData).rarm, "rarm");
            modelNameMap.put(((AnimationData) modelData).larm, "larm");
            modelNameMap.put(((AnimationData) modelData).rleg, "rleg");
            modelNameMap.put(((AnimationData) modelData).lleg, "lleg");
        }
    }

    public boolean isPart(HashMap<AnimationPartConfig, String> modelNameMap, AnimationPartConfig puppetPart, String partName) {
        return puppetPart.enablePart && partName.equals(modelNameMap.get(puppetPart));
    }

    public String getPartName(ModelRenderer renderer, HashMap<String,String[]> partNames) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;
        String returnName = "";

        while (returnName.isEmpty()) {
            for (Field f : RenderClass.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    if (renderer == f.get(model)) {
                        int i = 0;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            for (Map.Entry<String, String[]> entry : partNames.entrySet()) {
                String[] names = entry.getValue();
                for (String partName : names) {
                    try {
                        Field field = RenderClass.getDeclaredField(partName);
                        field.setAccessible(true);
                        if (renderer == field.get(model)) {
                            returnName = entry.getKey();
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (RenderClass == ModelBase.class || RenderClass.getSuperclass() == null) {
                break;
            }
            RenderClass = RenderClass.getSuperclass();
        }

        return returnName;
    }
}
