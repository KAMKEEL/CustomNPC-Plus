package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;
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
            if(isPart(modelData.head)){
                this.rotateAngleX = modelData.bipedRotsHead[0];
                this.rotateAngleY = modelData.bipedRotsHead[1];
                this.rotateAngleZ = modelData.bipedRotsHead[2];
                this.setInterpolatedAngles(modelData,modelData.head);
            }

            if(isPart(modelData.body)){
                this.rotateAngleX = modelData.bipedRotsBody[0];
                this.rotateAngleY = modelData.bipedRotsBody[1];
                this.rotateAngleZ = modelData.bipedRotsBody[2];
                this.setInterpolatedAngles(modelData,modelData.body);
            }

            if(isPart(modelData.larm)){
                this.rotateAngleX = modelData.bipedRotsLeftArm[0];
                this.rotateAngleY = modelData.bipedRotsLeftArm[1];
                this.rotateAngleZ = modelData.bipedRotsLeftArm[2];
                this.setInterpolatedAngles(modelData,modelData.larm);
            }

            if(isPart(modelData.rarm)){
                this.rotateAngleX = modelData.bipedRotsRightArm[0];
                this.rotateAngleY = modelData.bipedRotsRightArm[1];
                this.rotateAngleZ = modelData.bipedRotsRightArm[2];
                this.setInterpolatedAngles(modelData,modelData.rarm);
            }

            if(isPart(modelData.lleg)){
                this.rotateAngleX = modelData.bipedRotsLeftLeg[0];
                this.rotateAngleY = modelData.bipedRotsLeftLeg[1];
                this.rotateAngleZ = modelData.bipedRotsLeftLeg[2];
                this.setInterpolatedAngles(modelData,modelData.lleg);
            }

            if(isPart(modelData.rleg)){
                this.rotateAngleX = modelData.bipedRotsRightLeg[0];
                this.rotateAngleY = modelData.bipedRotsRightLeg[1];
                this.rotateAngleZ = modelData.bipedRotsRightLeg[2];
                this.setInterpolatedAngles(modelData,modelData.rleg);
            }

            if(isPart(modelData.head)){
                this.addInterpolatedOffset(modelData, modelData.head);
            }
            if(isPart(modelData.body)){
                this.addInterpolatedOffset(modelData, modelData.body);
            }
            if(isPart(modelData.larm)){
                this.addInterpolatedOffset(modelData, modelData.larm);
            }
            if(isPart(modelData.rarm)){
                this.addInterpolatedOffset(modelData, modelData.rarm);
            }
            if(isPart(modelData.lleg)){
                this.addInterpolatedOffset(modelData, modelData.lleg);
            }
            if(isPart(modelData.rleg)){
                this.addInterpolatedOffset(modelData, modelData.rleg);
            }

            if(isPart(modelData.head)) {
                modelData.bipedRotsHead = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
            if(isPart(modelData.body)) {
                modelData.bipedRotsBody = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
            if(isPart(modelData.larm)) {
                modelData.bipedRotsLeftArm = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
            if(isPart(modelData.rarm)) {
                modelData.bipedRotsRightArm = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
            if(isPart(modelData.lleg)) {
                modelData.bipedRotsLeftLeg = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
            if(isPart(modelData.rleg)) {
                modelData.bipedRotsRightLeg = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
            }
        }
    }

    public String getPartName(ModelRenderer renderer) {
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedHead
                || renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedHeadwear) {
            return "head";
        }
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedBody) {
            return "body";
        }
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedRightArm) {
            return "rarm";
        }
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedLeftArm) {
            return "larm";
        }
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedRightLeg) {
            return "rleg";
        }
        if (renderer == ((ModelBiped) ((ModelRenderer)renderer).baseModel).bipedLeftLeg) {
            return "lleg";
        }

        try {
            Class<?> ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");
            Object m = renderer.baseModel;

            if (renderer == (ModelRenderer) ModelBipedBody.getField("bipedHeadwear").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedHead").get(m)) {
                return "head";
            }
            if (renderer == (ModelRenderer) ModelBipedBody.getField("B1").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedBody").get(m)) {
                return "body";
            }
            if (renderer == (ModelRenderer) ModelBipedBody.getField("RA").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedRightArm").get(m)) {
                return "rarm";
            }
            if (renderer == (ModelRenderer) ModelBipedBody.getField("LA").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedLeftArm").get(m)) {
                return "larm";
            }
            if (renderer == (ModelRenderer) ModelBipedBody.getField("RL").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedRightLeg").get(m)) {
                return "rleg";
            }
            if (renderer == (ModelRenderer) ModelBipedBody.getField("LL").get(m)
                    || renderer == (ModelRenderer) ModelBipedBody.getField("bipedLeftLeg").get(m)) {
                return "lleg";
            }
        } catch (Exception ignored) {}

        return "";
    }

    public boolean isPart(JobPuppet.PartConfig puppetPart) {
        return !puppetPart.disabled && this.partName.equals(partNames.get(puppetPart));
    }

    public void setInterpolatedAngles(PlayerModelData modelData, JobPuppet.PartConfig puppetPart) {
        float pi = (float) Math.PI * (modelData.fullAngles ? 2 : 1);
        if (!modelData.animate) {
            this.rotateAngleX = puppetPart.rotationX * pi;
            this.rotateAngleY = puppetPart.rotationY * pi;
            this.rotateAngleZ = puppetPart.rotationZ * pi;
        } else if (puppetPart.partialRotationTick != ClientEventHandler.partialRenderTick) {
            puppetPart.partialRotationTick = ClientEventHandler.partialRenderTick;
            if (modelData.interpolate) {
                if (puppetPart.rotationX * pi - this.rotateAngleX != 0)
                    this.rotateAngleX = (puppetPart.rotationX * pi - this.rotateAngleX) * modelData.animRate / 10f + this.rotateAngleX;
                if (puppetPart.rotationY * pi - this.rotateAngleY != 0)
                    this.rotateAngleY = (puppetPart.rotationY * pi - this.rotateAngleY) * modelData.animRate / 10f + this.rotateAngleY;
                if (puppetPart.rotationZ * pi - this.rotateAngleZ != 0)
                    this.rotateAngleZ = (puppetPart.rotationZ * pi - this.rotateAngleZ) * modelData.animRate / 10f + this.rotateAngleZ;
            } else {
                int directionX = Float.compare(puppetPart.rotationX * pi, this.rotateAngleX);
                this.rotateAngleX += directionX * modelData.animRate / 10f;
                this.rotateAngleX = directionX == 1 ?
                        Math.min(puppetPart.rotationX * pi,this.rotateAngleX) : Math.max(puppetPart.rotationX * pi,this.rotateAngleX);
                int directionY = Float.compare(puppetPart.rotationY * pi, this.rotateAngleY);
                this.rotateAngleY += directionY * modelData.animRate / 10f;
                this.rotateAngleY = directionY == 1 ?
                        Math.min(puppetPart.rotationY * pi,this.rotateAngleY) : Math.max(puppetPart.rotationY * pi,this.rotateAngleY);
                int directionZ = Float.compare(puppetPart.rotationZ * pi, this.rotateAngleZ);
                this.rotateAngleZ += directionZ * modelData.animRate / 10f;
                this.rotateAngleZ = directionZ == 1 ?
                        Math.min(puppetPart.rotationZ * pi,this.rotateAngleZ) : Math.max(puppetPart.rotationZ * pi,this.rotateAngleZ);
            }
        }
    }

    public void addInterpolatedOffset(PlayerModelData modelData, JobPuppet.PartConfig puppetPart) {
        if (!puppetPart.setOriginalPivot) {
            puppetPart.setOriginalPivot = true;
            puppetPart.originalPivotX = this.rotationPointX;
            puppetPart.originalPivotY = this.rotationPointY;
            puppetPart.originalPivotZ = this.rotationPointZ;
        }

        if (!modelData.animate) {
            this.rotationPointX = puppetPart.originalPivotX + puppetPart.pivotX;
            this.rotationPointY = puppetPart.originalPivotY + puppetPart.pivotY;
            this.rotationPointZ = puppetPart.originalPivotZ + puppetPart.pivotZ;
        } else if (puppetPart.partialPivotTick != ClientEventHandler.partialRenderTick)  {
            puppetPart.partialPivotTick = ClientEventHandler.partialRenderTick;
            if (modelData.interpolate) {
                puppetPart.destPivotX = (puppetPart.pivotX - puppetPart.destPivotX) * modelData.animRate / 10f + puppetPart.destPivotX;
                this.rotationPointX = puppetPart.originalPivotX + puppetPart.destPivotX;
                puppetPart.destPivotY = (puppetPart.pivotY - puppetPart.destPivotY) * modelData.animRate / 10f + puppetPart.destPivotY;
                this.rotationPointY = puppetPart.originalPivotY + puppetPart.destPivotY;
                puppetPart.destPivotZ = (puppetPart.pivotZ - puppetPart.destPivotZ) * modelData.animRate / 10f + puppetPart.destPivotZ;
                this.rotationPointZ = puppetPart.originalPivotZ + puppetPart.destPivotZ;
            } else {
                int directionX = Float.compare(puppetPart.pivotX, this.rotationPointX);
                this.rotationPointX = puppetPart.originalPivotX + directionX * modelData.animRate / 10f;
                this.rotationPointX = directionX == 1 ?
                        Math.min(puppetPart.originalPivotX + puppetPart.pivotX,this.rotationPointX) : Math.max(puppetPart.originalPivotX + puppetPart.pivotX,this.rotationPointX);
                int directionY = Float.compare(puppetPart.pivotY, this.rotationPointY);
                this.rotationPointY = puppetPart.originalPivotY + directionY * modelData.animRate / 10f;
                this.rotationPointY = directionY == 1 ?
                        Math.min(puppetPart.originalPivotY + puppetPart.pivotY,this.rotationPointY) : Math.max(puppetPart.originalPivotY + puppetPart.pivotY,this.rotationPointY);
                int directionZ = Float.compare(puppetPart.pivotZ, this.rotationPointZ);
                this.rotationPointZ = puppetPart.originalPivotZ + directionZ * modelData.animRate / 10f;
                this.rotationPointZ = directionZ == 1 ?
                        Math.min(puppetPart.originalPivotZ + puppetPart.pivotZ,this.rotationPointZ) : Math.max(puppetPart.originalPivotZ + puppetPart.pivotZ,this.rotationPointZ);
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
