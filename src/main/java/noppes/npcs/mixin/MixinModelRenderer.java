package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.controllers.data.PlayerModelData;
import noppes.npcs.roles.JobPuppet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(value = ModelRenderer.class)
public class MixinModelRenderer {
    @Shadow public float rotationPointX;
    @Shadow public float rotationPointY;
    @Shadow public float rotationPointZ;
    @Shadow public float rotateAngleX;
    @Shadow public float rotateAngleY;
    @Shadow public float rotateAngleZ;

    HashMap<JobPuppet.PartConfig,String> partNames = new HashMap<>();
    String partName = "";

    @SideOnly(Side.CLIENT)
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void puppetRotations(float p_78785_1_, CallbackInfo callbackInfo)
    {
        if (ClientEventHandler.renderingPlayer == null) {
            return;
        }

        if (!Client.playerModelData.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
            return;
        }

        this.partName = this.getPartName((ModelRenderer)(Object)this);
        PlayerModelData modelData = Client.playerModelData.get(ClientEventHandler.renderingPlayer.getUniqueID());
        this.setModelParts(modelData);
        if (modelData.enabled) {
            JobPuppet.PartConfig[] partConfigs = new JobPuppet.PartConfig[]{modelData.head,modelData.body,modelData.larm,modelData.rarm,modelData.lleg,modelData.rleg};

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

    public String getPartName(ModelRenderer renderer) {
        if (renderer == ((ModelBiped) renderer.baseModel).bipedHead
                || renderer == ((ModelBiped) renderer.baseModel).bipedHeadwear) {
            return "head";
        }
        if (renderer == ((ModelBiped) renderer.baseModel).bipedBody) {
            return "body";
        }
        if (renderer == ((ModelBiped) renderer.baseModel).bipedRightArm) {
            return "rarm";
        }
        if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftArm) {
            return "larm";
        }
        if (renderer == ((ModelBiped) renderer.baseModel).bipedRightLeg) {
            return "rleg";
        }
        if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftLeg) {
            return "lleg";
        }

        try {
            Class<?> ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");
            Object m = renderer.baseModel;

            if (renderer == ModelBipedBody.getField("bipedHeadwear").get(m)
                    || renderer == ModelBipedBody.getField("bipedHead").get(m)) {
                return "head";
            }
            if (renderer == ModelBipedBody.getField("B1").get(m)
                    || renderer == ModelBipedBody.getField("bipedBody").get(m)) {
                return "body";
            }
            if (renderer == ModelBipedBody.getField("RA").get(m)
                    || renderer == ModelBipedBody.getField("bipedRightArm").get(m)) {
                return "rarm";
            }
            if (renderer == ModelBipedBody.getField("LA").get(m)
                    || renderer == ModelBipedBody.getField("bipedLeftArm").get(m)) {
                return "larm";
            }
            if (renderer == ModelBipedBody.getField("RL").get(m)
                    || renderer == ModelBipedBody.getField("bipedRightLeg").get(m)) {
                return "rleg";
            }
            if (renderer == ModelBipedBody.getField("LL").get(m)
                    || renderer == ModelBipedBody.getField("bipedLeftLeg").get(m)) {
                return "lleg";
            }
        } catch (Exception ignored) {}

        return "";
    }

    public boolean isPart(JobPuppet.PartConfig puppetPart) {
        return !puppetPart.disabled && this.partName.equals(partNames.get(puppetPart));
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

    public void setModelParts(PlayerModelData modelData) {
        partNames.put(modelData.head,"head");
        partNames.put(modelData.body,"body");
        partNames.put(modelData.rarm,"rarm");
        partNames.put(modelData.larm,"larm");
        partNames.put(modelData.rleg,"rleg");
        partNames.put(modelData.lleg,"lleg");
    }
}
