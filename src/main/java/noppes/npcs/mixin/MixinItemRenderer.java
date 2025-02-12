package noppes.npcs.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemInFirstPerson(float p_78440_1_, CallbackInfo callbackInfo)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Render renderer = RenderManager.instance.getEntityRenderObject(player);
        if (renderer instanceof RendererLivingEntity && ((RendererLivingEntity) renderer).mainModel instanceof ModelBiped) {
            Render playerRenderer = RenderManager.instance.getEntityRenderObject(player);
            if (playerRenderer instanceof RendererLivingEntity) {
                ClientEventHandler.renderer = (RendererLivingEntity) playerRenderer;
            }
            ClientEventHandler.partialRenderTick = Minecraft.getMinecraft().timer.renderPartialTicks;
            ClientEventHandler.partialHandTicks = p_78440_1_;

            ClientEventHandler.renderingPlayer = player;
            ClientEventHandler.firstPersonAnimation = true;
            ClientEventHandler.firstPersonModel = (ModelBiped) ((RendererLivingEntity) renderer).mainModel;
            if (mixin_renderFirstPersonAnimation(p_78440_1_, player, ClientEventHandler.firstPersonModel)) {
                callbackInfo.cancel();
            }
            ClientEventHandler.firstPersonAnimation = false;
            ClientEventHandler.renderingPlayer = null;
        }
    }

    private boolean mixin_renderFirstPersonAnimation(float partialRenderTick, EntityPlayer player, ModelBiped model) {
        AnimationData animationData = ClientCacheHandler.playerAnimations.get(player.getUniqueID());
        if (animationData != null && animationData.isActive()) {
            Frame frame = (Frame) animationData.animation.currentFrame();
            if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                part.interpolateOffset();
                part.interpolateAngles();
            }
        }

        ModelRenderer[] parts = new ModelRenderer[]{model.bipedRightArm, model.bipedLeftArm, model.bipedRightLeg, model.bipedLeftLeg};
        EnumAnimationPart[] enumParts = new EnumAnimationPart[]{EnumAnimationPart.RIGHT_ARM, EnumAnimationPart.LEFT_ARM, EnumAnimationPart.RIGHT_LEG, EnumAnimationPart.LEFT_LEG};
        Frame frame;

        if (animationData != null && animationData.isActive()) {
            Animation animation = (Animation) animationData.getAnimation();
            frame = (Frame) animation.currentFrame();
            for (int i = 0; i < parts.length; i++) {
                ModelRenderer part = parts[i];
                EnumAnimationPart enumPart = enumParts[i];
                FramePart originalPart = ClientEventHandler.originalValues.get(part);
                if (originalPart != null && frame.frameParts.containsKey(enumPart)) {
                    FramePart animatedPart = frame.frameParts.get(enumPart);
                    part.rotationPointX = originalPart.pivot[0] + animatedPart.prevPivots[0];
                    part.rotationPointY = originalPart.pivot[1] + animatedPart.prevPivots[1];
                    part.rotationPointZ = originalPart.pivot[2] + animatedPart.prevPivots[2];
                    part.rotateAngleX = animatedPart.prevRotations[0];
                    part.rotateAngleY = animatedPart.prevRotations[1];
                    part.rotateAngleZ = animatedPart.prevRotations[2];
                }
            }
        } else {
            return false;
        }

        if (animationData != null && animationData.isActive()) {
            if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                float pi = 180 / (float) Math.PI;
                GL11.glRotatef(-part.prevRotations[1] * pi, 0, 1, 0);
            }
        }

        EntityClientPlayerMP entityclientplayermp = Minecraft.getMinecraft().thePlayer;
        RenderHelper.enableStandardItemLighting();
        int i = Minecraft.getMinecraft().theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        float f3 = entityclientplayermp.prevRenderArmPitch + (entityclientplayermp.renderArmPitch - entityclientplayermp.prevRenderArmPitch) * partialRenderTick;
        float f4 = entityclientplayermp.prevRenderArmYaw + (entityclientplayermp.renderArmYaw - entityclientplayermp.prevRenderArmYaw) * partialRenderTick;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);

        try {
            this.dbc_render(player);
        } catch (Exception e) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(entityclientplayermp.getLocationSkin());

            this.renderLimbs();
        }

        ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;
        ItemStack itemstack = itemRenderer.itemToRender;
        if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM) && itemstack != null)
        {
            float f11, f12;
            GL11.glPushMatrix();

            if (player.fishEntity != null)
            {
                itemstack = new ItemStack(Items.stick);
            }

            float p_78785_1_ = 0.0625F;
            GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

            GL11.glTranslatef(model.bipedRightArm.rotationPointX * p_78785_1_, model.bipedRightArm.rotationPointY * p_78785_1_, model.bipedRightArm.rotationPointZ * p_78785_1_);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleZ), 0, 0, 1);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleY), 0, 1, 0);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleX), 1, 0, 0);
            GL11.glTranslatef(-0.1F, 0.6F, 0.0F);

            GL11.glRotatef(255, 0, 1, 0);
            GL11.glRotatef(45, 1, 0, 0);
            GL11.glRotatef(80, 0, 0, 1);

            float f6 = 1/1.5F;
            GL11.glScalef(f6, f6, f6);

            if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                for (k = 0; k < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); ++k)
                {
                    i = itemstack.getItem().getColorFromItemStack(itemstack, k);
                    f12 = (float)(i >> 16 & 255) / 255.0F;
                    f3 = (float)(i >> 8 & 255) / 255.0F;
                    f4 = (float)(i & 255) / 255.0F;
                    GL11.glColor4f(f12, f3, f4, 1.0F);
                    mixin_renderItem(player, itemstack, k, EQUIPPED_FIRST_PERSON);
                }
            }
            else
            {
                k = itemstack.getItem().getColorFromItemStack(itemstack, 0);
                f11 = (float)(k >> 16 & 255) / 255.0F;
                f12 = (float)(k >> 8 & 255) / 255.0F;
                f3 = (float)(k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                mixin_renderItem(player, itemstack, 0, EQUIPPED_FIRST_PERSON);
            }

            GL11.glPopMatrix();
        }

        for (int p = 0; p < parts.length; p++) {
            ModelRenderer part = parts[p];
            EnumAnimationPart enumPart = enumParts[p];
            FramePart originalPart = ClientEventHandler.originalValues.get(part);
            if (originalPart != null && frame.frameParts.containsKey(enumPart)) {
                part.rotationPointX = originalPart.pivot[0];
                part.rotationPointY = originalPart.pivot[1];
                part.rotationPointZ = originalPart.pivot[2];
                part.rotateAngleX = originalPart.rotation[0];
                part.rotateAngleY = originalPart.rotation[1];
                part.rotateAngleZ = originalPart.rotation[2];
            }
        }

        return true;
    }

    private void renderLimbs() {
        AnimationData animationData = ClientCacheHandler.playerAnimations.get(Minecraft.getMinecraft().thePlayer.getUniqueID());
        if (animationData != null && animationData.isActive() && animationData.getAnimation() != null && animationData.getAnimation().currentFrame() != null) {
            Animation animation = (Animation) animationData.getAnimation();
            Frame frame = (Frame) animation.currentFrame();

            ModelBiped model = ClientEventHandler.firstPersonModel;

            if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedRightArm.render(0.0625F);
                GL11.glPopMatrix();
            }


            if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_ARM)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedLeftArm.render(0.0625F);
                GL11.glPopMatrix();
            }


            if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_LEG)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.025F, 0.6F + -0.75F * 0.8F, -0.4F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedRightLeg.render(0.0625F);
                GL11.glPopMatrix();
            }

            if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_LEG)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.025F, 0.6F + -0.75F * 0.8F, -0.4F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedLeftLeg.render(0.0625F);
                GL11.glPopMatrix();
            }
        }
    }

    private void dbc_render(EntityPlayer par1EntityPlayer) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Minecraft mc = Minecraft.getMinecraft();

        Class<?> JBRAH = Class.forName("JinRyuu.JBRA.JBRAH");
        Class<?> JRMCoreHDBC = Class.forName("JinRyuu.JRMCore.JRMCoreHDBC");
        Class<?> JRMCoreH = Class.forName("JinRyuu.JRMCore.JRMCoreH");
        Class<?> ExtendedPlayer = Class.forName("JinRyuu.JRMCore.i.ExtendedPlayer");
        Class<?> JGConfigClientSettings = Class.forName("JinRyuu.JRMCore.client.config.jrmc.JGConfigClientSettings");
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Class<?> JGConfigRaces = Class.forName("JinRyuu.JRMCore.server.config.dbc.JGConfigRaces");

        boolean jhdsBool = (boolean) JBRAH.getMethod("JHDS").invoke(null);
        boolean dbcBool = (boolean) JRMCoreH.getMethod("DBC").invoke(null);

        RenderPlayer renderPlayer = (RenderPlayer) RenderManager.instance.getEntityRenderObject(par1EntityPlayer);
        ModelBiped modelMain = (ModelBiped) RenderPlayerJBRA.getField("modelMain").get(renderPlayer);

        EntityClientPlayerMP acp = mc.thePlayer;

        Object data = null;
        if (jhdsBool) {
            data = JBRAH.getMethod("skinData", EntityPlayer.class).invoke(null, acp);
        }

        float f = 1.0F;
        GL11.glColor3f(f + getR(), f + getG(), f + getB());
        GL11.glPushMatrix();
        //modelMain.field_78095_p = 0.0F;
        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
        String dns = (String) JRMCoreH.getField("dns").get(null);
        if (dns.length() > 3) {
            int State = (byte) JRMCoreH.getField("State").get(null);
            int race = (int) JRMCoreH.getMethod("dnsRace", String.class).invoke(null, dns);
            boolean saiOozar = (boolean) JRMCoreH.getMethod("rSai", int.class).invoke(null, race) && (State == 7 || State == 8);
            int gen = (int) JRMCoreH.getMethod("dnsGender", String.class).invoke(null, dns);
            int skintype = (int) JRMCoreH.getMethod("dnsSkinT", String.class).invoke(null, dns);
            boolean lg = (boolean) JRMCoreH.getMethod("lgndb", EntityPlayer.class, int.class, int.class).invoke(null, par1EntityPlayer, race, State);
            boolean iau = (boolean) JRMCoreH.getMethod("rc_arc", int.class).invoke(null, race) && State == 6;
            String dnsau = (String) JRMCoreH.getMethod("data", int.class, String.class).invoke(null, 16, "");
            dnsau = dnsau.contains(";") ? dnsau.substring(1) : (par1EntityPlayer.getCommandSenderName().equals(mc.thePlayer.getCommandSenderName()) ? dnsau : "");
            int bodytype = skintype == 0 ? (int) JRMCoreH.getMethod("dnsBodyC1_0", String.class).invoke(null, dns) : (int) JRMCoreH.getMethod("dnsBodyT", String.class).invoke(null, dns);
            int bodycm = skintype == 0 ? 0 : (iau ? (int) JRMCoreH.getMethod("dnsauCM", String.class).invoke(null, dns) : (int) JRMCoreH.getMethod("dnsBodyCM", String.class).invoke(null, dns));
            int bodyc1 = skintype == 0 ? 0 : (iau ? (int) JRMCoreH.getMethod("dnsauC1", String.class).invoke(null, dns) : (int) JRMCoreH.getMethod("dnsBodyC1", String.class).invoke(null, dns));
            int bodyc2 = skintype == 0 ? 0 : (iau ? (int) JRMCoreH.getMethod("dnsauC2", String.class).invoke(null, dns) : (int) JRMCoreH.getMethod("dnsBodyC2", String.class).invoke(null, dns));
            int bodyc3 = skintype == 0 ? 0 : (iau ? (int) JRMCoreH.getMethod("dnsauC3", String.class).invoke(null, dns) : (int) JRMCoreH.getMethod("dnsBodyC3", String.class).invoke(null, dns));

            int plyrSpc = skintype == 0 ? 0 : (((int[]) JRMCoreH.getField("RaceCustomSkin").get(null))[race] == 0 ? 0 : (bodytype >= ((int[]) JRMCoreH.getField("Specials").get(null))[race] ? ((int[]) JRMCoreH.getField("Specials").get(null))[race] - 1 : bodytype));
            int[] an = new int[]{1, 0, 2, 0, 0, 3, 0, 1, 1};

            Object ep = ExtendedPlayer.getMethod("get", EntityPlayer.class).invoke(null, acp);
            int animKiShoot = (int) ExtendedPlayer.getMethod("getAnimKiShoot").invoke(ep);
            int blocking = (int) ExtendedPlayer.getMethod("getBlocking").invoke(ep);

            boolean instantTransmission = blocking == 2;
            int id = blocking != 0 ? (instantTransmission ? 6 : 0) : (animKiShoot != 0 ? an[animKiShoot - 1] + 2 : -1);
            if (!(boolean)JGConfigClientSettings.getField("CLIENT_DA4").get(null)) {
                id = -1;
            }

            int tailCol;

            int jx;
            int j;
            if (dbcBool) {
                String[] s = ((String) JRMCoreH.getMethod("data", String.class, int.class, String.class).invoke(null,acp.getCommandSenderName(), 1, "0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0")).split(";");
                tailCol = Integer.parseInt(s[2]);
                if (tailCol == 1) {
                    String[] PlyrSkills = (String[]) JRMCoreH.getMethod("PlyrSkills", EntityPlayer.class).invoke(null, acp);
                    jx = (int) JRMCoreH.getMethod("SklLvl", int.class, String[].class).invoke(null,12, PlyrSkills);
                    j = (int) JRMCoreH.getMethod("SklLvl", int.class, String[].class).invoke(null,15, PlyrSkills);
                    String ss = s[17];
                    boolean v = dbcBool && !ss.equals("-1");
                    GL11.glPushMatrix();
                    if (v && (jx > 0 || j > 0)) {
                        if (id > -1) {
                            RenderPlayerJBRA.getMethod("func_aam", int.class, boolean.class, boolean.class).invoke(renderPlayer, id, false, true);
                        }

                        GL11.glRotatef(6.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(-0.29F, 0.15F, 0.0F);
                        RenderPlayerJBRA.getMethod("kss", Entity.class, boolean.class, int.class, int.class, int.class).invoke(
                            null, acp, false, Integer.parseInt(ss), jx, j);
                    }

                    GL11.glPopMatrix();
                }
            }

            float h1 = 1.0F;
            ResourceLocation bdyskn;
            boolean ssg;
            boolean v;
            if (race == 5 && dbcBool) {
                v = State == 1;
                ssg = State == 3 && (boolean) JGConfigRaces.getField("CONFIG_MAJIN_PURE_PINK_SKIN").get(null);
                if (v) {
                    bodycm = 12561588;
                } else if (ssg) {
                    bodycm = 16757199;
                }

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/majin/" + (gen == 1 ? "f" : "") + "majin.png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                String[] playerData13 = ((String) JRMCoreH.getMethod("data", String.class, int.class, String.class).invoke(null,
                    par1EntityPlayer.getCommandSenderName(), 13, "0;0;0;0,0,0+0")).split(";");
                String[] absorptionData;
                if (playerData13.length > 3) {
                    absorptionData = playerData13[3].split(",");
                } else {
                    absorptionData = "0;0;0;0,0,0+0".split(",");
                }

                String[] absorptionVisuals = absorptionData[1].contains("+") ? absorptionData[1].split("\\+") : new String[]{absorptionData[1]};
                int absorbedRace = Integer.parseInt(absorptionVisuals[0]);
                if ((boolean) JRMCoreH.getMethod("isRaceArcosian", int.class).invoke(null, absorbedRace)
                    || (boolean) JRMCoreH.getMethod("isRaceNamekian", int.class).invoke(null, absorbedRace)) {
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/majin/" + (gen == 1 ? "f" : "") + "majin_" + ((boolean) JRMCoreH.getMethod("isRaceArcosian", int.class).invoke(null, absorbedRace) ? "arco" : "namek") + ".png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(bodycm);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();
                }

                if (!saiOozar) {
                    if (skintype == 0) {
                        bdyskn = acp.getLocationSkin().equals(ClientEventHandler.steveTextures) ? (gen >= 1 ? ClientEventHandler.fem : ClientEventHandler.steveTextures) : acp.getLocationSkin();
                        if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data)) {
                            mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        } else {
                            mc.getTextureManager().bindTexture(bdyskn);
                        }

                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }
                }
            } else if (race == 3 && dbcBool) {
                v = (boolean) JRMCoreH.getMethod("StusEfctsMe", int.class).invoke(null, 17);
                ssg = (boolean) JRMCoreHDBC.getMethod("godKiUserBase", int.class, int.class).invoke(null, race, State);
                if (ssg && v) {
                    bodycm = 16744999;
                    bodyc1 = 15524763;
                    bodyc2 = 12854822;
                    bodyc3 = 0;
                }

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/0nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/1nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/2nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/3nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();
            } else if (race == 4 && dbcBool) {
                v = (boolean) JRMCoreH.getMethod("StusEfctsMe", int.class).invoke(null, 17);
                ssg = (boolean) JRMCoreHDBC.getMethod("godKiUserBase", int.class, int.class).invoke(null, race, State);
                if (ssg && v) {
                    State = 6;
                    bodycm = 5526612;
                    bodyc1 = 12829635;
                    bodyc3 = 1513239;
                }

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/0A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/1A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/2A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/3A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc3);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/4A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();
            } else {
                if (saiOozar) {
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru1.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(skintype != 0 ? bodycm : 11374471);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();

                    tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                    jx = State != 0 && State != 7 ? (lg ? 10092390 : 16574610) : (skintype == 1 ? bodyc1 : tailCol);
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru2.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(jx);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();
                } else if (skintype != 0) {
                    bdyskn = new ResourceLocation("jinryuumodscore:cc/" + (gen == 1 ? "f" : "") + "hum.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(bodycm);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();
                }

                if (!saiOozar) {
                    if (skintype == 0) {
                        bdyskn = acp.getLocationSkin().equals(ClientEventHandler.steveTextures) ? (gen >= 1 ? ClientEventHandler.fem : ClientEventHandler.steveTextures) : acp.getLocationSkin();
                        if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data)) {
                            mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        } else {
                            mc.getTextureManager().bindTexture(bdyskn);
                        }

                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }

                    if (State == 14) {
                        tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                        tailCol = (boolean)JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null) ? 13292516 : tailCol;
                        tailCol = skintype == 1 ? bodyc1 : tailCol;
                        if ((boolean) JRMCoreH.getMethod("rSai", int.class).invoke(null, race) && tailCol == 6498048 && State == 14) {
                            if ((boolean)JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null)) {
                                tailCol = 13292516;
                            } else {
                                tailCol = 14292268;
                            }
                        }

                        mc.getTextureManager().bindTexture(new ResourceLocation("jinryuudragonbc:cc/ss4" + (skintype == 0 ? "a" : "b") + ".png"));
                        glColor3f(tailCol);
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }
                }
            }

            if ((boolean) JGConfigClientSettings.getField("CLIENT_DA19").get(null) && (dbcBool || (boolean) JRMCoreH.getMethod("NC").invoke(null))) {
                GL11.glPushMatrix();
                GL11.glEnable(3042);
                GL11.glDisable(2896);
                GL11.glBlendFunc(770, 771);
                GL11.glAlphaFunc(516, 0.003921569F);
                GL11.glDepthMask(false);
                tailCol = Integer.parseInt((String) JRMCoreH.getMethod("data", String.class, int.class, String.class
                    ).invoke(null, par1EntityPlayer.getCommandSenderName(), 8, "200"));
                float one = (float)tailCol / 100.0F;
                j = (int)((float)tailCol / one);
                if (j < 70) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises1.png"));
                    this.renderLimbs();
                }

                if (j < 55) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises2.png"));
                    this.renderLimbs();
                }

                if (j < 35) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises3.png"));
                    this.renderLimbs();
                }

                if (j < 20) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises4.png"));
                    this.renderLimbs();
                }

                GL11.glDepthMask(true);
                GL11.glEnable(2896);
                GL11.glDisable(3042);
                GL11.glPopMatrix();
            }

            ItemStack itemstack = par1EntityPlayer.inventory.armorItemInSlot(2);
            String dbcarmor;
            Item item;
            ResourceLocation mcarmor;
            ResourceLocation armor;
            ItemArmor itemarmor;
            if (itemstack != null) {
                item = itemstack.getItem();
                if (item instanceof ItemArmor) {
                    itemarmor = (ItemArmor)item;
                    GL11.glPushMatrix();
                    dbcarmor = itemarmor.getArmorTexture(itemstack, par1EntityPlayer, 2, null);
                    mcarmor = RenderBiped.getArmorResource(par1EntityPlayer, itemstack, 1, null);
                    if (dbcarmor != null) {
                        dbcarmor = dbcarmor.replace("jbra", "").replace("_dam", "");
                    }

                    armor = dbcarmor != null ? new ResourceLocation(dbcarmor) : mcarmor;
                    mc.getTextureManager().bindTexture(armor);
                    GL11.glPushMatrix();
                    if (id > -1) {
                        RenderPlayerJBRA.getMethod("func_aam", int.class, boolean.class, boolean.class).invoke(renderPlayer, id, false, true);
                    }

                    GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                    GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                    if (dbcarmor != null) {
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        modelMain.textureHeight = 64;
                        modelMain.textureWidth = 128;
                        this.renderLimbs();
                    } else {
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }

                    GL11.glPopMatrix();
                    GL11.glPopMatrix();
                }
            }

            if (race == 3 && dbcBool) {
                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/0nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/1nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/2nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/3nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();
            } else if (race == 4 && dbcBool) {
                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/0A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/1A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/2A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/3A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc3);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/4A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                this.renderLimbs();
            } else {
                if (saiOozar) {
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru1.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(skintype != 0 ? bodycm : 11374471);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();

                    jx = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                    j = State != 0 && State != 7 ? (lg ? 10092390 : 16574610) : (skintype == 1 ? bodyc1 : jx);
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru2.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(j);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();
                } else if (skintype != 0) {
                    bdyskn = new ResourceLocation("jinryuumodscore:cc/" + (gen == 1 ? "f" : "") + "hum.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(bodycm);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    this.renderLimbs();
                }

                if (!saiOozar) {
                    if (skintype == 0) {
                        bdyskn = acp.getLocationSkin().equals(ClientEventHandler.steveTextures) ? (gen >= 1 ? ClientEventHandler.fem : ClientEventHandler.steveTextures) : acp.getLocationSkin();
                        if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data)) {
                            mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        } else {
                            mc.getTextureManager().bindTexture(bdyskn);
                        }

                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));

                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }

                    if (State == 14) {
                        tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                        tailCol = (boolean)JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null) ? 13292516 : tailCol;
                        jx = skintype == 1 ? bodyc1 : tailCol;
                        if ((boolean) JRMCoreH.getMethod("rSai", int.class).invoke(null, race) && jx == 6498048 && State == 14) {
                            if ((boolean)JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null)) {
                                jx = 13292516;
                            } else {
                                jx = 14292268;
                            }
                        }

                        mc.getTextureManager().bindTexture(new ResourceLocation("jinryuudragonbc:cc/ss4" + (skintype == 0 ? "a" : "b") + ".png"));
                        glColor3f(jx);
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        this.renderLimbs();
                    }
                }
            }

            if ((boolean) JGConfigClientSettings.getField("CLIENT_DA19").get(null)) {
                GL11.glPushMatrix();
                GL11.glEnable(3042);
                GL11.glDisable(2896);
                GL11.glBlendFunc(770, 771);
                GL11.glAlphaFunc(516, 0.003921569F);
                GL11.glDepthMask(false);
                tailCol = (int) JRMCoreH.getMethod(
                    "stat", Entity.class, int.class, int.class, int.class, int.class, int.class, int.class, float.class).invoke(null,
                    par1EntityPlayer, 2,
                    (byte) JRMCoreH.getField("Pwrtyp").get(null), 2,
                    ((int[]) JRMCoreH.getField("PlyrAttrbts").get(null))[2], race,
                    (byte) JRMCoreH.getField("Class").get(null), 0.0F);
                jx = Integer.parseInt((String) JRMCoreH.getMethod("data", String.class, int.class, String.class).invoke(null, par1EntityPlayer.getCommandSenderName(), 8, "200"));
                float one = (float)tailCol / 100.0F;
                int perc = (int)((float)jx / one);
                if (perc < 70) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises1.png"));
                    this.renderLimbs();
                }

                if (perc < 55) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises2.png"));
                    this.renderLimbs();
                }

                if (perc < 35) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises3.png"));
                    this.renderLimbs();
                }

                if (perc < 20) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises4.png"));
                    this.renderLimbs();
                }

                GL11.glDepthMask(true);
                GL11.glEnable(2896);
                GL11.glDisable(3042);
                GL11.glPopMatrix();
            }

            if (itemstack != null) {
                item = itemstack.getItem();
                if (item instanceof ItemArmor) {
                    itemarmor = (ItemArmor)item;
                    GL11.glPushMatrix();
                    dbcarmor = itemarmor.getArmorTexture(itemstack, par1EntityPlayer, 2, null);
                    mcarmor = RenderBiped.getArmorResource(par1EntityPlayer, itemstack, 1, null);
                    if (dbcarmor != null) {
                        dbcarmor = dbcarmor.replace("jbra", "").replace("_dam", "");
                    }

                    armor = dbcarmor != null ? new ResourceLocation(dbcarmor) : mcarmor;
                    mc.getTextureManager().bindTexture(armor);
                    if (id == 0 || id == 3 || id == 5) {
                        if (id == 0) {
                            if ((boolean) JGConfigClientSettings.getField("CLIENT_DA18").get(null)) {
                                GL11.glPushMatrix();
                                RenderPlayerJBRA.getMethod("func_aam", int.class, boolean.class, boolean.class).invoke(renderPlayer, id, false, true);
                                GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                                GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                                if (dbcarmor != null) {
                                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                    modelMain.textureHeight = 64;
                                    modelMain.textureWidth = 128;
                                    this.renderLimbs();
                                } else {
                                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                    this.renderLimbs();
                                }

                                GL11.glPopMatrix();
                            }
                        } else {
                            GL11.glPushMatrix();
                            RenderPlayerJBRA.getMethod("func_aam", int.class, boolean.class, boolean.class).invoke(renderPlayer, id, false, true);
                            GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                            GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                            if (dbcarmor != null) {
                                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                modelMain.textureHeight = 64;
                                modelMain.textureWidth = 128;
                                this.renderLimbs();
                            } else {
                                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                this.renderLimbs();
                            }

                            GL11.glPopMatrix();
                        }
                    }

                    GL11.glPopMatrix();
                }
            }
        }

        GL11.glPopMatrix();
    }

    private float getR() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getR");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private float getB() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getB");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private float getG() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getG");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private void glColor3f(int c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        float h2 = (float)(c >> 16 & 255) / 255.0F;
        float h3 = (float)(c >> 8 & 255) / 255.0F;
        float h4 = (float)(c & 255) / 255.0F;
        float h1 = 1.0F;
        float r = h1 * h2;
        float g = h1 * h3;
        float b = h1 * h4;
        GL11.glColor3f(r + getR(), g + getG(), b + getB());
    }

    @Shadow private RenderBlocks renderBlocksIr;
    @Shadow private static ResourceLocation RES_ITEM_GLINT;

    private void mixin_renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type) {
        GL11.glPushMatrix();
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        Item item = p_78443_2_.getItem();
        Block block = Block.getBlockFromItem(item);

        if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(p_78443_2_, type);
        if (customRenderer != null) {
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            ForgeHooksClient.renderEquippedItem(type, customRenderer, renderBlocksIr, p_78443_1_, p_78443_2_);
        } else if (p_78443_2_.getItemSpriteNumber() == 0 && item instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType())) {
            texturemanager.bindTexture(texturemanager.getResourceLocation(0));

            GL11.glTranslatef(0.0F, 0.2F,-0.2F);
            if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0) {
                GL11.glDepthMask(false);
                renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
                GL11.glDepthMask(true);
            } else {
                renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
            }
        } else {
            IIcon iicon = p_78443_1_.getItemIcon(p_78443_2_, p_78443_3_);

            if (iicon == null) {
                GL11.glPopMatrix();
                return;
            }

            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            float f = iicon.getMinU();
            float f1 = iicon.getMaxU();
            float f2 = iicon.getMinV();
            float f3 = iicon.getMaxV();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

            if (p_78443_2_.hasEffect(p_78443_3_)) {
                GL11.glDepthFunc(GL11.GL_EQUAL);
                GL11.glDisable(GL11.GL_LIGHTING);
                texturemanager.bindTexture(RES_ITEM_GLINT);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(768, 1, 1, 0);
                float f7 = 0.76F;
                GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glPushMatrix();
                float f8 = 0.125F;
                GL11.glScalef(f8, f8, f8);
                float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
                GL11.glTranslatef(f9, 0.0F, 0.0F);
                GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glScalef(f8, f8, f8);
                f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
                GL11.glTranslatef(-f9, 0.0F, 0.0F);
                GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_147945_b();
        }

        GL11.glPopMatrix();
    }
}
