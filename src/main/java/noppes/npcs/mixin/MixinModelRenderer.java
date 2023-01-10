package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.controllers.data.PlayerModelData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.roles.JobPuppet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = ModelRenderer.class)
public class MixinModelRenderer {
    @Shadow public float rotationPointX;
    @Shadow public float rotationPointY;
    @Shadow public float rotationPointZ;
    @Shadow public float rotateAngleX;
    @Shadow public float rotateAngleY;
    @Shadow public float rotateAngleZ;

    HashMap<JobPuppet.PartConfig,String> modelNameMap = new HashMap<>();
    String partName = "";
    HashMap<String,String[]> partNames = new HashMap<>();

    @SideOnly(Side.CLIENT)
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void puppetRotations(float p_78785_1_, CallbackInfo callbackInfo)
    {
        if (partNames.isEmpty()) {
            String[] headNames = new String[]{"bipedHead","bipedHeadwear","head","Head","bipedHeadAll",
                    "bipedHeadg","bipedHeadt","bipedHeadgh","bipedHeadv","bipedHeadb","bipedHeadt2"};
            String[] bodyNames = new String[]{"bipedBody","B1","body","UpperBody","Body1","BodyBase"};
            String[] larmNames = new String[]{"bipedLeftArm","LA","leftarm","ArmL","Arm1L","ArmL1"};
            String[] rarmNames = new String[]{"bipedRightArm","RA","rightarm","ArmR","Arm1R","ArmR1"};
            String[] llegNames = new String[]{"bipedLeftLeg","LL","leftleg","LegL","Leg1L","LegL1"};
            String[] rlegNames = new String[]{"bipedRightLeg","RL","rightleg","LegR","Leg1R","LegR1"};

            partNames.put("head", headNames);
            partNames.put("body", bodyNames);
            partNames.put("larm", larmNames);
            partNames.put("rarm", rarmNames);
            partNames.put("lleg", llegNames);
            partNames.put("rleg", rlegNames);
        }

        if (ClientEventHandler.renderingPlayer != null && Client.playerModelData.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
            this.partName = this.getPartName((ModelRenderer) (Object) this, this.partNames);
            PlayerModelData modelData = Client.playerModelData.get(ClientEventHandler.renderingPlayer.getUniqueID());
            this.setModelParts(modelData);
            if (modelData.enabled) {
                JobPuppet.PartConfig[] partConfigs = new JobPuppet.PartConfig[]{modelData.head, modelData.body, modelData.larm, modelData.rarm, modelData.lleg, modelData.rleg};

                for (JobPuppet.PartConfig partConfig : partConfigs) {
                    if (isPart(partConfig)) {
                        this.rotateAngleX = partConfig.prevRotations[0];
                        this.rotateAngleY = partConfig.prevRotations[1];
                        this.rotateAngleZ = partConfig.prevRotations[2];
                        this.setInterpolatedAngles(partConfig);
                        this.addInterpolatedOffset(partConfig);
                        partConfig.prevRotations = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
                    }
                }
            }
        }
        if (ClientEventHandler.renderingNpc != null && ClientEventHandler.renderingNpc.jobInterface instanceof JobPuppet) {
            this.partName = this.getPartName((ModelRenderer) (Object) this, this.partNames);
            JobPuppet modelData = (JobPuppet) ClientEventHandler.renderingNpc.jobInterface;
            this.setModelParts(modelData);
            if (modelData.isActive()) {
                JobPuppet.PartConfig[] partConfigs = new JobPuppet.PartConfig[]{modelData.head, modelData.body, modelData.larm, modelData.rarm, modelData.lleg, modelData.rleg};

                for (JobPuppet.PartConfig partConfig : partConfigs) {
                    if (isPart(partConfig)) {
                        EntityLivingBase entityModel = ((EntityCustomNpc) ClientEventHandler.renderingNpc).modelData.getEntity(ClientEventHandler.renderingNpc);
                        if (partConfig.npcModel != entityModel) {
                            partConfig.setOriginalPivot = false;
                        }
                        this.rotateAngleX = partConfig.prevRotations[0];
                        this.rotateAngleY = partConfig.prevRotations[1];
                        this.rotateAngleZ = partConfig.prevRotations[2];
                        this.setInterpolatedAngles(partConfig);
                        this.addInterpolatedOffset(partConfig);
                        partConfig.prevRotations = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
                        partConfig.npcModel = entityModel;
                    }
                }
            }
        }
    }

    public String getPartName(ModelRenderer renderer, HashMap<String,String[]> partNames) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;

        for (Map.Entry<String,String[]> entry : partNames.entrySet()) {
            String[] names = entry.getValue();
            for (String partName : names) {
                try {
                    Field field = RenderClass.getDeclaredField(partName);
                    field.setAccessible(true);
                    if (renderer == field.get(model)) {
                        return entry.getKey();
                    }
                } catch (Exception ignored) {}
            }
        }

        return "";
    }

    public boolean isPart(JobPuppet.PartConfig puppetPart) {
        return !puppetPart.disabled && this.partName.equals(modelNameMap.get(puppetPart));
    }

    public void setInterpolatedAngles(JobPuppet.PartConfig modelPart) {
        float pi = (float) Math.PI * (modelPart.fullAngles ? 2 : 1);
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
                        Math.min(modelPart.rotationX * pi,this.rotateAngleX) : Math.max(modelPart.rotationX * pi,this.rotateAngleX);
                int directionY = Float.compare(modelPart.rotationY * pi, this.rotateAngleY);
                this.rotateAngleY += directionY * Math.abs(modelPart.animRate) / 10f;
                this.rotateAngleY = directionY == 1 ?
                        Math.min(modelPart.rotationY * pi,this.rotateAngleY) : Math.max(modelPart.rotationY * pi,this.rotateAngleY);
                int directionZ = Float.compare(modelPart.rotationZ * pi, this.rotateAngleZ);
                this.rotateAngleZ += directionZ * Math.abs(modelPart.animRate) / 10f;
                this.rotateAngleZ = directionZ == 1 ?
                        Math.min(modelPart.rotationZ * pi,this.rotateAngleZ) : Math.max(modelPart.rotationZ * pi,this.rotateAngleZ);
            }
        }
    }

    public void addInterpolatedOffset(JobPuppet.PartConfig modelPart) {
        if (!modelPart.setOriginalPivot) {
            modelPart.setOriginalPivot = true;
            modelPart.originalPivotX = this.rotationPointX;
            modelPart.originalPivotY = this.rotationPointY;
            modelPart.originalPivotZ = this.rotationPointZ;
        }

        if (!modelPart.animate) {
            this.rotationPointX = modelPart.originalPivotX + modelPart.pivotX;
            this.rotationPointY = modelPart.originalPivotY + modelPart.pivotY;
            this.rotationPointZ = modelPart.originalPivotZ + modelPart.pivotZ;
        } else if (modelPart.partialPivotTick != ClientEventHandler.partialRenderTick)  {
            modelPart.partialPivotTick = ClientEventHandler.partialRenderTick;
            if (modelPart.interpolate) {
                modelPart.destPivotX = (modelPart.pivotX - modelPart.destPivotX) * Math.abs(modelPart.animRate) / 10f + modelPart.destPivotX;
                this.rotationPointX = modelPart.originalPivotX + modelPart.destPivotX;
                modelPart.destPivotY = (modelPart.pivotY - modelPart.destPivotY) * Math.abs(modelPart.animRate) / 10f + modelPart.destPivotY;
                this.rotationPointY = modelPart.originalPivotY + modelPart.destPivotY;
                modelPart.destPivotZ = (modelPart.pivotZ - modelPart.destPivotZ) * Math.abs(modelPart.animRate) / 10f + modelPart.destPivotZ;
                this.rotationPointZ = modelPart.originalPivotZ + modelPart.destPivotZ;
            } else {
                int directionX = Float.compare(modelPart.pivotX, this.rotationPointX);
                this.rotationPointX = modelPart.originalPivotX + directionX * Math.abs(modelPart.animRate) / 10f;
                this.rotationPointX = directionX == 1 ?
                        Math.min(modelPart.originalPivotX + modelPart.pivotX,this.rotationPointX) : Math.max(modelPart.originalPivotX + modelPart.pivotX,this.rotationPointX);
                int directionY = Float.compare(modelPart.pivotY, this.rotationPointY);
                this.rotationPointY = modelPart.originalPivotY + directionY * Math.abs(modelPart.animRate) / 10f;
                this.rotationPointY = directionY == 1 ?
                        Math.min(modelPart.originalPivotY + modelPart.pivotY,this.rotationPointY) : Math.max(modelPart.originalPivotY + modelPart.pivotY,this.rotationPointY);
                int directionZ = Float.compare(modelPart.pivotZ, this.rotationPointZ);
                this.rotationPointZ = modelPart.originalPivotZ + directionZ * Math.abs(modelPart.animRate) / 10f;
                this.rotationPointZ = directionZ == 1 ?
                        Math.min(modelPart.originalPivotZ + modelPart.pivotZ,this.rotationPointZ) : Math.max(modelPart.originalPivotZ + modelPart.pivotZ,this.rotationPointZ);
            }
        }
    }

    public void setModelParts(Object modelData) {
        if (modelData instanceof PlayerModelData) {
            modelNameMap.put(((PlayerModelData) modelData).head, "head");
            modelNameMap.put(((PlayerModelData) modelData).body, "body");
            modelNameMap.put(((PlayerModelData) modelData).rarm, "rarm");
            modelNameMap.put(((PlayerModelData) modelData).larm, "larm");
            modelNameMap.put(((PlayerModelData) modelData).rleg, "rleg");
            modelNameMap.put(((PlayerModelData) modelData).lleg, "lleg");
        }
        if (modelData instanceof JobPuppet) {
            modelNameMap.put(((JobPuppet) modelData).head, "head");
            modelNameMap.put(((JobPuppet) modelData).body, "body");
            modelNameMap.put(((JobPuppet) modelData).rarm, "rarm");
            modelNameMap.put(((JobPuppet) modelData).larm, "larm");
            modelNameMap.put(((JobPuppet) modelData).rleg, "rleg");
            modelNameMap.put(((JobPuppet) modelData).lleg, "lleg");
        }
    }
}
