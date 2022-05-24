package noppes.npcs.client.renderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SkinOverlay;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class RenderCNPCPlayer extends RenderPlayer {
    public OverlayRenderHelper itemRenderer = new OverlayRenderHelper(Minecraft.getMinecraft());
    public float tempRenderPartialTicks;
    private float debugCamFOV;
    private float prevDebugCamFOV;
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float fovMultiplierTemp;

    public RenderCNPCPlayer() {
        super();
        this.modelBipedMain = (ModelBiped)this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
        this.setRenderManager(RenderManager.instance);
    }

    private boolean preRenderOverlay(EntityPlayer player, ResourceLocation overlayLocation, boolean glow,
                                     float alpha, float size, float speedX, float speedY, float scaleX, float scaleY,
                                     float offsetX, float offsetY, float offsetZ) {
        try {
            this.bindTexture(overlayLocation);
        } catch (Exception exception) {
            return false;
        }

        if (!Client.entitySkinOverlayTicks.containsKey(player.getUniqueID())) {
            Client.entitySkinOverlayTicks.put(player.getUniqueID(), 1L);
        } else {
            long ticks = Client.entitySkinOverlayTicks.get(player.getUniqueID());
            Client.entitySkinOverlayTicks.put(player.getUniqueID(), ticks + 1);
        }
        float partialTickTime = Client.entitySkinOverlayTicks.get(player.getUniqueID());

        // Overlay & Glow
        if (glow) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
            Minecraft.getMinecraft().entityRenderer.disableLightmap((double)0);
            RenderHelper.disableStandardItemLighting();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        GL11.glDepthMask(!player.isInvisible());

        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glTranslatef(partialTickTime * 0.001F * speedX, partialTickTime * 0.001F * speedY, 0.0F);
        GL11.glScalef(scaleX, scaleY, 1.0F);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glTranslatef(offsetX, offsetY, offsetZ);
        GL11.glScalef(size, size, size);

        return true;
    }
    public void postRenderOverlay(EntityPlayer player) {
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        Minecraft.getMinecraft().entityRenderer.enableLightmap((double) 0);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        EntityPlayer player = (EntityPlayer) p_77036_1_;

        if (!p_77036_1_.isInvisible())
        {
            if (Client.skinOverlays.containsKey(player.getUniqueID())) {
                for (SkinOverlay overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                    if (overlayData.location == null) {
                        overlayData.location = new ResourceLocation(overlayData.texture);
                    } else {
                        String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                        if (!str.equals(overlayData.texture)) {
                            overlayData.location = new ResourceLocation(overlayData.texture);
                        }
                    }

                    if (!preRenderOverlay(player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
                            overlayData.speedX, overlayData.speedY, overlayData.scaleX, overlayData.scaleY,
                            overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ
                            ))
                        return;
                    try {
                        renderEquippedItemsJBRA(player, tempRenderPartialTicks);
                    } catch (Exception e) {
                        this.modelBipedMain.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    } finally {
                        postRenderOverlay(player);
                    }
                }
            } else if(player.getEntityData().hasKey("SkinOverlayData")) {
                Client.sendData(EnumPacketServer.SERVER_UPDATE_SKIN_OVERLAYS, new Object[0]);
            }
        }
    }
    
    public void renderHand(float partialTicks, int renderPass) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;
        EntityClientPlayerMP player = mc.thePlayer;

        if (entityRenderer.debugViewDirection <= 0)
        {
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float f1 = 0.07F;

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(-(renderPass * 2 - 1)) * f1, 0.0F, 0.0F);
            }

            /*if (entityRenderer.cameraZoom != 1.0D) //EntityRenderer's "cameraZoom" field is always 1.0D??? Why is this here??? o_O
            {
                GL11.glTranslatef((float)player.cameraYaw, (float)(-player.cameraPitch), 0.0F);
                GL11.glScaled(entityRenderer.cameraZoom, entityRenderer.cameraZoom, 1.0D);
            }*/

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, mc.gameSettings.renderDistanceChunks * 16 * 2.0F);

            if (mc.playerController.enableEverythingIsScrewedUpMode())
            {
                float f2 = 0.6666667F;
                GL11.glScalef(1.0F, f2, 1.0F);
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(renderPass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            GL11.glPushMatrix();
            hurtCameraEffect(partialTicks);

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI && !mc.playerController.enableEverythingIsScrewedUpMode())
            {
                itemRenderer.updateEquippedItem();
                entityRenderer.enableLightmap((double)partialTicks);
                itemRenderer.renderItemInFirstPerson(partialTicks);
                entityRenderer.disableLightmap((double)partialTicks);
            }

            GL11.glPopMatrix();

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping())
            {
                entityRenderer.itemRenderer.renderOverlays(partialTicks);
                hurtCameraEffect(partialTicks);
            }

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }
        }
    }

    private float getFOVModifier(float p_78481_1_, boolean p_78481_2_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entityplayer = (EntityLivingBase)mc.renderViewEntity;
        float f1 = 70.0F;

        if (p_78481_2_)
        {
            f1 = mc.gameSettings.fovSetting;
            f1 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * p_78481_1_;
        }

        if (entityplayer.getHealth() <= 0.0F)
        {
            float f2 = (float)entityplayer.deathTime + p_78481_1_;
            f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entityplayer, p_78481_1_);

        if (block.getMaterial() == Material.water)
        {
            f1 = f1 * 60.0F / 70.0F;
        }

        return f1 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * p_78481_1_;
    }

    public void updateFovModifierHand()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayerSP)
        {
            EntityPlayerSP entityplayersp = (EntityPlayerSP)mc.renderViewEntity;
            this.fovMultiplierTemp = entityplayersp.getFOVMultiplier();
        }
        else
        {
            this.fovMultiplierTemp = mc.thePlayer.getFOVMultiplier();
        }
        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F)
        {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F)
        {
            this.fovModifierHand = 0.1F;
        }
    }

    private void hurtCameraEffect(float p_78482_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entitylivingbase = mc.renderViewEntity;
        float f1 = (float)entitylivingbase.hurtTime - p_78482_1_;
        float f2;

        if (entitylivingbase.getHealth() <= 0.0F)
        {
            f2 = (float)entitylivingbase.deathTime + p_78482_1_;
            GL11.glRotatef(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
        }

        if (f1 >= 0.0F)
        {
            f1 /= (float)entitylivingbase.maxHurtTime;
            f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float)Math.PI);
            f2 = entitylivingbase.attackedAtYaw;
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    private void setupViewBobbing(float p_78475_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)mc.renderViewEntity;
            float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f2 = -(entityplayer.distanceWalkedModified + f1 * p_78475_1_);
            float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * p_78475_1_;
            float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * p_78475_1_;
            GL11.glTranslatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float)Math.PI) * f3), 0.0F);
            GL11.glRotatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float)Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);
        }
    }

    public void renderFirstPersonArm(EntityPlayer player, float partialTickTime)
    {
        Render render = RenderManager.instance.getEntityRenderObject(player);
        RenderPlayer renderplayer = (RenderPlayer)render;
        renderplayer.renderFirstPersonArm(player);

        if (Client.skinOverlays.containsKey(player.getUniqueID())) {
            for (SkinOverlay overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                if (overlayData.location == null) {
                    overlayData.location = new ResourceLocation(overlayData.texture);
                } else {
                    String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                    if (!str.equals(overlayData.texture)) {
                        overlayData.location = new ResourceLocation(overlayData.texture);
                    }
                }

                if (!preRenderOverlay(player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
                        overlayData.speedX, overlayData.speedY, overlayData.scaleX, overlayData.scaleY,
                        overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ
                ))
                    return;
                this.modelBipedMain.onGround = 0.0F;
                this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
                this.modelBipedMain.bipedRightArm.render(0.0625F);
                postRenderOverlay(player);
            }
        } else if(player.getEntityData().hasKey("SkinOverlayData")) {
            Client.sendData(EnumPacketServer.SERVER_UPDATE_SKIN_OVERLAYS, new Object[0]);
        }
    }

    byte b(String n) {
        return Byte.parseByte(n);
    }

    protected void renderEquippedItemsJBRA(EntityPlayer player, float partialTick) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Minecraft mc = Minecraft.getMinecraft();
        Object data = null;

        Class<?> renderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Class<?> JBRAH = Class.forName("JinRyuu.JBRA.JBRAH");
        Class<?> JRMCoreH = Class.forName("JinRyuu.JRMCore.JRMCoreH");
        Class<?> JGConfigUltraInstinct = Class.forName("JinRyuu.JRMCore.server.config.dbc.JGConfigUltraInstinct");
        Class<?> JRMCoreHDBC = Class.forName("JinRyuu.JRMCore.JRMCoreHDBC");
        Class<?> ExtendedPlayer = Class.forName("JinRyuu.JRMCore.i.ExtendedPlayer");
        Class<?> ItemVanity = Class.forName("JinRyuu.JRMCore.items.ItemVanity");
        Class<?> ItemsDBC = Class.forName("JinRyuu.DragonBC.common.Items.ItemsDBC");
        Class<?> JRMCoreConfig = Class.forName("JinRyuu.JRMCore.JRMCoreConfig");
        Class<?> JRMCoreGuiScreen = Class.forName("JinRyuu.JRMCore.JRMCoreGuiScreen");
        Class<?> ModelBipedDBC = Class.forName("JinRyuu.JBRA.ModelBipedDBC");
        Class<?> JRMCoreHJYC = Class.forName("JinRyuu.JRMCore.JRMCoreHJYC");
        Class<?> JGConfigClientSettings = Class.forName("JinRyuu.JRMCore.client.config.jrmc.JGConfigClientSettings");

        Object playerRenderer = renderPlayerJBRA.cast(RenderManager.instance.getEntityRenderObject(player));

        Object modelMain = renderPlayerJBRA.getField("modelMain").get(playerRenderer);
        ModelBiped model = ((ModelBiped) modelMain);

        float headOffsetY = 0.00051F;
        model.bipedHead.offsetY += headOffsetY;

        float armOffsetX = -0.00125F;
        model.bipedLeftArm.offsetX += armOffsetX;
        model.bipedRightArm.offsetX += -armOffsetX;

        float legOffsetX = -0.0005F;
        float legOffsetY = -0.002F;
        float legOffsetZ = 0.0F;
        if (player.isSneaking()) {
            legOffsetZ += -0.00075F;
        }
        model.bipedLeftLeg.offsetX += legOffsetX;
        model.bipedRightLeg.offsetX += -legOffsetX;
        model.bipedLeftLeg.offsetY += legOffsetY;
        model.bipedRightLeg.offsetY += legOffsetY;
        model.bipedLeftLeg.offsetZ += legOffsetZ;
        model.bipedRightLeg.offsetZ += legOffsetZ;

        if ((Boolean) JBRAH.getMethod("JHDS").invoke(null)) {
            data = JBRAH.getMethod("skinData",EntityPlayer.class).invoke(null,player);
        }

        ItemStack itemstack = player.inventory.armorItemInSlot(3);
        boolean doit = true;
        ItemStack hair = player.inventory.armorItemInSlot(3);
        if (hair != null) {
            if ((Boolean) JRMCoreH.getMethod("DBC").invoke(null)) {
                doit = true;
            } else if ((Boolean) JRMCoreH.getMethod("NC").invoke(null) && itemstack.getItem() instanceof ItemArmor) {
                if (hair.getItem().getUnlocalizedName().endsWith("Headband")) {
                    doit = false;
                }

                if (hair.getItem().getUnlocalizedName().replaceAll("item.", "").startsWith("akatsuki")) {
                    doit = true;
                }
            } else {
                doit = true;
            }
        } else {
            doit = true;
        }

        boolean dbc = (Boolean) JRMCoreH.getMethod("DBC").invoke(null);
        boolean nc = (Boolean) JRMCoreH.getMethod("NC").invoke(null);
        boolean saoc = (Boolean) JRMCoreH.getMethod("SAOC").invoke(null);
        float age = (float) JRMCoreHJYC.getMethod("JYCAge",EntityPlayer.class).invoke(null,player);
        int pl;
        if (JRMCoreH.getField("plyrs").get(null) != null && ((String[]) JRMCoreH.getField("plyrs").get(null)).length > 0 && !player.isInvisible() && (Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,1) && ((Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,2) && (Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,4) && (Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,5) && (Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,19) || !dbc && !saoc && !nc)) {
            for(pl = 0; pl < ((String[]) JRMCoreH.getField("plyrs").get(null)).length; ++pl) {
                if (((String[]) JRMCoreH.getField("plyrs").get(null))[pl].equals(player.getCommandSenderName())) {
                    String[] s = ((String[]) JRMCoreH.getField("data1").get(null))[pl].split(";");
                    int powerType = Integer.parseInt(s[2]);
                    int race = Integer.parseInt(s[0]);
                    String[] dummy = new String[]{"0", "0", "0"};
                    int rg = JRMCoreH.getField("data4").get(null) == null ? 0 : Integer.parseInt(((String[]) JRMCoreH.getField("data4").get(null))[pl].split(";")[0]);
                    String[] state = JRMCoreH.getField("data2").get(null) == null ? dummy : ((String[]) JRMCoreH.getField("data2").get(null))[pl].split(";");
                    String dns = s[1];
                    int classID = Integer.parseInt(s[3]);
                    int weight = Integer.parseInt(s[5].split(",")[0]);
                    int st = (Boolean) JRMCoreH.getMethod("rc_arc",int.class).invoke(null,race) && (Boolean) JRMCoreGuiScreen.getField("ufc").get(null) ? 6 : (powerType != 2 && race != 0 ? this.b(state[0]) : 0);
                    int stY = this.b(state[0]);
                    boolean saiOozar = (Boolean) JRMCoreH.getMethod("rSai",int.class).invoke(null,race) && (st == 7 || st == 8);
                    int gen = (Integer) JRMCoreH.getMethod("dnsGender",String.class).invoke(null,dns);
                    int haircol = (Integer) JRMCoreH.getMethod("dnsHairC",String.class).invoke(null,dns);
                    int hairback = (Integer) JRMCoreH.getMethod("dnsHairB",String.class).invoke(null,dns);
                    int breast = (Integer) JRMCoreH.getMethod("dnsBreast",String.class).invoke(null,dns);
                    int skintype = (Integer) JRMCoreH.getMethod("dnsSkinT",String.class).invoke(null,dns);
                    boolean iau = (Boolean) JRMCoreH.getMethod("rc_arc",int.class).invoke(null,race) && st == 6;
                    String dnsau = (String) JRMCoreH.getMethod("data",int.class, int.class, String.class).invoke(null,pl, 16, "");
                    dnsau = dnsau.contains(";") ? dnsau.substring(1) : (((String[]) JRMCoreH.getField("plyrs").get(null))[pl].equals(mc.thePlayer.getCommandSenderName()) ? dnsau : "");
                    int bodytype = skintype == 0 ? (Integer) JRMCoreH.getMethod("dnsBodyC1_0",String.class).invoke(null,dns) : (Integer) JRMCoreH.getMethod("dnsBodyT",String.class).invoke(null,dns);
                    int bodycm = skintype == 0 ? 0 : (iau ? (Integer) JRMCoreH.getMethod("dnsauCM",String.class).invoke(null,dnsau) : (Integer) JRMCoreH.getMethod("dnsBodyCM",String.class).invoke(null,dns));
                    int bodyc1 = skintype == 0 ? 0 : (iau ? (Integer) JRMCoreH.getMethod("dnsauC1",String.class).invoke(null,dnsau) : (Integer) JRMCoreH.getMethod("dnsBodyC1",String.class).invoke(null,dns));
                    int bodyc2 = skintype == 0 ? 0 : (iau ? (Integer) JRMCoreH.getMethod("dnsauC2",String.class).invoke(null,dnsau) : (Integer) JRMCoreH.getMethod("dnsBodyC2",String.class).invoke(null,dns));
                    int bodyc3 = skintype == 0 ? 0 : (iau ? (Integer) JRMCoreH.getMethod("dnsauC3",String.class).invoke(null,dnsau) : (Integer) JRMCoreH.getMethod("dnsBodyC3",String.class).invoke(null,dns));
                    int facen = skintype == 0 ? 0 : (Integer) JRMCoreH.getMethod("dnsFaceN",String.class).invoke(null,dns);
                    int facem = skintype == 0 ? 0 : (Integer) JRMCoreH.getMethod("dnsFaceM",String.class).invoke(null,dns);
                    int eyes = skintype == 0 ? 0 : (Integer) JRMCoreH.getMethod("dnsEyes",String.class).invoke(null,dns);
                    int eyec1 = skintype == 0 ? 0 : (Integer) JRMCoreH.getMethod("dnsEyeC1",String.class).invoke(null,dns);
                    int eyec2 = skintype == 0 ? 0 : (Integer) JRMCoreH.getMethod("dnsEyeC2",String.class).invoke(null,dns);
                    if (JRMCoreH.getField("data5").get(null) != null) {
                        ((String[])JRMCoreH.getField("data5").get(null))[pl].split(";");
                    }

                    HashMap<Integer,String[]> playerData = new HashMap<>();
                    for (int i = 0; i <= 31; i++) {
                        Object obj;

                        if (i < 10) {
                            obj = JRMCoreH.getField("data" + i).get(null);
                        } else {
                            obj = JRMCoreH.getField("dat" + i).get(null);
                        }

                        if (obj != null) {
                            playerData.put(i, ((String[]) obj)[pl].split(";"));
                        }
                    }

                    int[] playerAttributes = new int[((int[])(JRMCoreH.getField("PlyrAttrbts").get(null))).length];
                    String[] playerStatsStr = ((String[])(JRMCoreH.getField("dat14").get(null)))[pl].split(",");

                    for(int i = 0; i < playerAttributes.length; ++i) {
                        playerAttributes[i] = Integer.parseInt(playerStatsStr[i]);
                    }

                    byte release = Byte.parseByte(playerData.get(10)[0]);
                    float f1 = 0.9375F;
                    float f2 = 1.0F;
                    float f3 = 1.0F;
                    boolean noC = !(Boolean) JRMCoreH.getMethod("DBC").invoke(null);

                    if ((int)JRMCoreH.getMethod("dnsGender",String.class).invoke(null,(String)JRMCoreH.getField("dns").get(null)) <= 1) {
                        f1 = 0.73F + (noC ? 0.27F : 0.0F);
                    }

                    if ((int)JRMCoreH.getMethod("dnsGender",String.class).invoke(null,(String)JRMCoreH.getField("dns").get(null)) >= 2) {
                        f1 = 0.7F + (noC ? 0.27F : 0.0F);
                    }

                    float yc = 1.0F;
                    float f1r;
                    if ((Boolean) JRMCoreH.getMethod("JYC").invoke(null)) {
                        f1r = age;
                        if (f1r <= 6.0F) {
                            yc = 0.5F;
                        }

                        if (f1r > 6.0F && f1r <= 52.0F) {
                            yc = (3.0F - (1.0F + (1.0F - (f1r - 6.0F) / 46.0F))) * 0.5F;
                        }

                        if (f1r > 53.0F) {
                            yc = 1.0F;
                        }

                        yc = Math.max(yc, 0.54347825F);
                    }

                    if ((Boolean) JRMCoreH.getMethod("DBC").invoke(null) && (Boolean) JRMCoreConfig.getField("sizes").get(null)) {
                        f1r = f1;
                        f1 += (float) JRMCoreHDBC.getMethod("DBCsizeBasedOnCns",int[].class).invoke(null, (Object) playerAttributes);

                        if (!((Boolean) JRMCoreH.getMethod("isPowerTypeChakra",int.class).invoke(null,powerType))) {
                            f2 = (float) JRMCoreHDBC.getMethod("DBCsizeBasedOnRace",int.class,int.class).invoke(null, race, st);
                            f3 = (float) JRMCoreHDBC.getMethod("DBCsizeBasedOnRace2",int.class,int.class).invoke(null, race, st);
                        }

                        if ((Boolean) JRMCoreH.getMethod("rSai",int.class).invoke(null,race) && (st == 7 || st == 8)) {
                            release = 50;
                            f1 = f1r;
                        }

                        float f3a = (f3 - 1.0F) * (float)release * 0.02F + 1.0F;
                        f3 = f3a > f3 ? f3 : (f3 > 1.0F ? f3a : f3);
                        float f2a = (f2 - 1.0F) * (float)release * 0.02F + 1.0F;
                        f2 = f2 > 1.0F ? f2a : f2;
                        float f1a1 = (f1 - f1r) * (release <= 50 ? 0.25F : 0.5F);
                        float f1ac = f1a1 * (float)release * 0.02F;
                        f1 = f1 - f1r - f1a1 + f1ac + f1r;
                        String Fzn = (String) JRMCoreH.getMethod("getString",EntityPlayer.class,String.class).invoke(null,player,"jrmcFuzion");
                        if (Fzn.contains(",")) {
                            String[] FznA = Fzn.split(",");
                            if (FznA.length == 3) {
                                boolean isp2 = FznA[1].equalsIgnoreCase(player.getCommandSenderName());
                                if (isp2) {
                                    f1 = 0.0F;
                                }
                            }
                        }
                    }

                    float clientHeight = f1 * f3 * yc;
                    float clientWidth2 = f1 * f2 * f3 * yc;

                    GL11.glTranslatef(0,-1.60859F * clientHeight + 1.50766F,0);
                    GL11.glScalef(clientWidth2 * 1.07F,clientHeight * 1.07F,clientWidth2 * 1.07F);

                    boolean lg = (Boolean)JRMCoreH.getMethod("lgndb",int.class,int.class,int.class).invoke(null,pl, race, st);
                    boolean v = (Boolean)JRMCoreH.getMethod("StusEfctsClient",int.class,int.class).invoke(null,17, pl);
                    boolean l = (Boolean)JRMCoreH.getMethod("StusEfctsClient",int.class,int.class).invoke(null,19, pl);
                    int hc = haircol;
                    boolean ultra_instinct_color = false;

                    byte CONFIG_UI_LEVELS = (Byte) JGConfigUltraInstinct.getField("CONFIG_UI_LEVELS").get(null);

                    if ((Boolean) JRMCoreH.getMethod("DBC").invoke(null) && l && CONFIG_UI_LEVELS > 0) {
                        byte id = CONFIG_UI_LEVELS < this.b(state[1]) ? CONFIG_UI_LEVELS : this.b(state[1]);
                        int ultra_instinct_level = (Integer) JRMCoreH.getMethod("state2UltraInstinct",boolean.class,int.class).invoke(null,false, id);
                        ultra_instinct_color = ((Boolean[])JGConfigUltraInstinct.getField("CONFIG_UI_HAIR_WHITE").get(null))[ultra_instinct_level];
                    }
                    
                    int suphcol = ((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && dbc ? ((int)JRMCoreHDBC.getMethod("col_fe",int.class,int.class,int.class,int.class,int.class,boolean.class,boolean.class,boolean.class,boolean.class).invoke(null,0, 0, powerType, race, st, v, lg, l, ultra_instinct_color)) : (dbc && l && ultra_instinct_color ? ((int)JRMCoreHDBC.getMethod("col_fe",int.class,int.class,int.class,int.class,int.class,boolean.class,boolean.class,boolean.class,boolean.class).invoke(null,0, 0, powerType, race, st, v, lg, l, ultra_instinct_color)) : 0);
                    int supecoll = dbc ? ((int)JRMCoreHDBC.getMethod("col_fe",int.class, int.class, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class).invoke(null,1, eyec1, powerType, race, stY, v, lg, l)) : eyec1;
                    int supecolr = dbc ? ((int)JRMCoreHDBC.getMethod("col_fe",int.class, int.class, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class).invoke(null,1, eyec2, powerType, race, stY, v, lg, l)) : eyec2;
                    String[] StE = JRMCoreH.getField("dat19").get(null) == null ? dummy : ((String[])JRMCoreH.getField("dat19").get(null))[pl].split(";");

                    byte ts = Byte.parseByte(StE[0]);
                    boolean mj = (boolean) JRMCoreH.getMethod("StusEfctsClient",int.class,int.class).invoke(null,12,pl);
                    boolean msk = (boolean) JRMCoreH.getMethod("StusEfctsClient",int.class,int.class).invoke(null,6,pl);
                    Object props = ExtendedPlayer.getMethod("get",EntityPlayer.class).invoke(null,player);
                    String dnsH = ((String)ExtendedPlayer.getMethod("getHairCode").invoke(props)).length() > 5 ? ((String)ExtendedPlayer.getMethod("getHairCode").invoke(props)) : "";
                    dnsH = (String) JRMCoreH.getMethod("dnsHairG1toG2",String.class).invoke(null,dnsH);

                    if ((Boolean) JRMCoreH.getMethod("DBC").invoke(null)) {

                        boolean kk2 = (boolean) JRMCoreH.getMethod("StusEfctsMe",int.class).invoke(null,5);
                        renderPlayerJBRA.getField("kk").set(playerRenderer,this.b(state[1]) + 1);

                        int kk = (int) renderPlayerJBRA.getField("kk").get(playerRenderer);


                        boolean rAccessible = renderPlayerJBRA.getField("r2").isAccessible();
                        boolean gAccessible = renderPlayerJBRA.getField("g2").isAccessible();
                        boolean bAccessible = renderPlayerJBRA.getField("b2").isAccessible();

                        renderPlayerJBRA.getField("r2").setAccessible(true);
                        renderPlayerJBRA.getField("g2").setAccessible(true);
                        renderPlayerJBRA.getField("b2").setAccessible(true);

                        if (kk2) {
                            renderPlayerJBRA.getField("r2").set(null,(float)kk / 15.0F);
                            renderPlayerJBRA.getField("g2").set(null,-((float)kk / 15.0F));
                            renderPlayerJBRA.getField("b2").set(null,-((float)kk / 15.0F));
                            if ((float)renderPlayerJBRA.getField("r2").get(null) > 1.0F) {
                                renderPlayerJBRA.getField("r2").set(null,1.0F);
                            }

                            if ((float)renderPlayerJBRA.getField("g2").get(null) < 0.0F) {
                                renderPlayerJBRA.getField("r2").set(null,0.0F);
                            }

                            if ((float)renderPlayerJBRA.getField("b2").get(null) < 0.0F) {
                                renderPlayerJBRA.getField("r2").set(null,0.0F);
                            }
                        } else {
                            renderPlayerJBRA.getField("r2").set(null,0.0F);
                            renderPlayerJBRA.getField("g2").set(null,0.0F);
                            renderPlayerJBRA.getField("b2").set(null,0.0F);
                        }

                        renderPlayerJBRA.getField("r2").setAccessible(rAccessible);
                        renderPlayerJBRA.getField("g2").setAccessible(gAccessible);
                        renderPlayerJBRA.getField("b2").setAccessible(bAccessible);
                    }

                    GL11.glPushMatrix();
                    int maxBody;
                    float h1;
                    if (race == 3 && dbc) {
                        int j = 5095183;
                        h1 = 1.0F;
                        ////renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                        ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"N");

                        ////renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                        ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);

                        ////renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc1);
                        //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);


                        ////renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc2);
                        //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        
                        //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                        //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACENOSE");
                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACEMOUTH");
                        //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBASE");
                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec1 : supecoll);
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYELEFT");
                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec2 : supecolr);
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBROW");
                    } else {
                        if (race == 4 && dbc) {
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);

                            short TransFrHrn = ((short[]) JRMCoreH.getField("TransFrHrn").get(null))[st];

                            ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,(ts == 4 ? "n" : "") + "FR" + TransFrHrn);
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc1);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,(ts == 4 ? "n" : "") + "FR" + TransFrHrn);


                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc1);
                            ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc2);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,(ts == 4 ? "n" : "") + "FR" + TransFrHrn);

                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc2);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc3);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,(ts == 4 ? "n" : "") + "FR" + TransFrHrn);

                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc3);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);

                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                            h1 = 1.0F;
                            //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,(ts == 4 ? "n" : "") + "FR" + TransFrHrn);

                            //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc1);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACENOSE");
                            if (st == 5 && msk) {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACEMOUTH");
                            } else {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodyc1);
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACEMOUTH");
                            }

                            //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBASE");
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec1 : supecoll);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYELEFT");
                            //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec2 : supecolr);
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                        } else {
                            int tailCol;
                            boolean bc;
                            if (saiOozar) {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,skintype != 0 ? bodycm : 11374471);
                                ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                                maxBody = st != 0 && st != 7 ? (lg ? 10092390 : 16574610) : (skintype == 1 ? bodyc1 : tailCol);
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,maxBody);
                                //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBASE");
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,skintype != 0 ? bodycm : 11374471);
                                ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"OOZARU");
                            } else if (skintype != 0) {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                                ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACENOSE");
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,bodycm);
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"FACEMOUTH");
                                //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBASE");
                                if (supecoll > 0) {
                                    if (powerType == 2 && classID == 1) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,15590377);
                                    } else if (powerType == 2 && classID == 2 && stY > 0) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,13828096);
                                    } else {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!dbc || !((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec1 : supecoll);
                                    }

                                    //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYELEFT");
                                }

                                if (supecolr > 0) {
                                    if (powerType == 2 && classID == 1) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,15590377);
                                    } else if (powerType == 2 && classID == 2 && stY > 0) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,13828096);
                                    } else {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,!dbc || !((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && !((Boolean)JRMCoreHDBC.getMethod("godKiUserBase",int.class,int.class).invoke(null,race,stY)) && !l ? eyec2 : supecolr);
                                    }

                                    //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                                }

                                if (race != 1 && race != 2) {
                                    bc = false;
                                } else if (Integer.parseInt(state[0]) != 6) {
                                    bc = false;
                                } else {
                                    bc = true;
                                }

                                if (!bc) {
                                    if (l && ultra_instinct_color) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class,float.class).invoke(null,15790320, age);
                                    } else if ((race == 1 || race == 2) && st != 0) {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,suphcol);
                                    } else {
                                        //renderPlayerJBRA.getMethod("glColor3f",int.class,float.class).invoke(null,haircol, age);
                                    }
                                } else {
                                    //GL11.glColor3f(1.0F, 1.0F, 1.0F);
                                }

                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYEBROW");
                            }

                            if (((Boolean)JRMCoreH.getMethod("rc_sai",int.class).invoke(null,race)) && dbc) {
                                tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                                maxBody = st != 0 && st != 7 && st != 14 ? suphcol : (skintype == 1 ? bodyc1 : tailCol);
                                if ((Boolean) JRMCoreH.getMethod("rSai",int.class).invoke(null,race) && maxBody == 6498048) {
                                    if (st == 14) {
                                        maxBody = (Boolean) JRMCoreH.getMethod("func_26n6").invoke(null) ? 13292516 : 14292268;
                                    } else if (l && ultra_instinct_color) {
                                        maxBody = 15790320;
                                    }
                                }

                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,maxBody);
                                ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,ts != 0 && ts != -1 ? (ts == 1 ? "SJT2" : "") : "SJT1");
                            }

                            h1 = 1.0F;
                            if (!saiOozar) {
                                if (skintype == 0 && gen >= 1) {
                                    //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                                    //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                } else if ((Boolean) JBRAH.getMethod("JHDS").invoke(null) && (Boolean) JBRAH.getMethod("getSkinHas",Object.class).invoke(null,data) && skintype == 0) {
                                    //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                                    //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                }
                            }

                            boolean var10000;
                            label964: {
                                label963: {
                                    if (l) {
                                        if (!ultra_instinct_color) {
                                            break label963;
                                        }
                                    } else if (st == 0) {
                                        break label963;
                                    }

                                    if (st != 14) {
                                        var10000 = true;
                                        break label964;
                                    }
                                }

                                var10000 = false;
                            }

                            bc = var10000;
                            String s1;
                            if ((hairback == 8 || hairback == 9) && (st == 0 || st == 1)) {
                                hc = bc ? suphcol : haircol;
                                s1 = hairback == 8 ? "c2" : "c1";
                            } else if (hairback >= 0 && hairback <= 12) {
                                hc = bc ? suphcol : (st == 14 && (Boolean) JRMCoreH.getMethod("func_26n6").invoke(null) ? 13292516 : haircol);
                                s1 = st == 0 ? "normall" : "superall";
                            }

                            if (bc) {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,hc);
                            } else {
                                //renderPlayerJBRA.getMethod("glColor3f",int.class,float.class).invoke(null,hc,age);
                            }

                            boolean scouter = false;
                            boolean helmet = false;
                            if (itemstack != null) {
                                Item item = itemstack.getItem(); ;
                                if ((Boolean) JRMCoreH.getMethod("DBC").invoke(null) && item instanceof ItemArmor) {
                                    ItemArmor itemarmor = (ItemArmor)item;
                                    helmet = true;
                                }
                            }

                            boolean vanity_head = false;
                            String[][] slot_vanity_num = new String[8][];
                            int[] slot_van = new int[8];

                            for(int i = 0; i < 8; ++i) {
                                slot_vanity_num[i] = s[8 + i].split(",");
                                slot_van[i] = Integer.parseInt(slot_vanity_num[i][0]);
                                if (!vanity_head && slot_van[i] > 0) {
                                    new Item();
                                    Item vanity_check = Item.getItemById(slot_van[i]);
                                    if (ItemVanity.isInstance(vanity_check) && (int)(ItemVanity.getField("armorType").get(ItemVanity.cast(vanity_check))) == 5) {
                                        int var90 = slot_van[i];
                                        Item var10001 = (Item)ItemsDBC.getField("Coat_2").get(null);
                                        if (var90 != Item.getIdFromItem((Item)ItemsDBC.getField("Coat_2").get(null))) {
                                            var90 = slot_van[i];
                                            var10001 = (Item)ItemsDBC.getField("Coat").get(null);
                                            if (var90 != Item.getIdFromItem((Item)ItemsDBC.getField("Coat").get(null))) {
                                                vanity_head = true;
                                            }
                                        }
                                    }
                                }
                            }

                            if (!saiOozar) {
                                boolean dhhwho = !((Boolean) JRMCoreConfig.getField("HHWHO").get(null)) || !helmet && !vanity_head || scouter;
                                if (hairback == 12 && dhhwho && dnsH.length() > 3) {
                                    if (player == mc.thePlayer && (Integer)JRMCoreGuiScreen.getField("hairPreview").get(null) > 0) {
                                        st = ((int[])JRMCoreGuiScreen.getField("hairPreviewStates").get(null))[(Integer)JRMCoreGuiScreen.getField("hairPreview").get(null)];
                                    }
                                    
                                    if (st == 6) {
                                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"" + ((String[])JRMCoreH.getField("HairsT").get(null))[6] + ((String[])JRMCoreH.getField("Hairs").get(null))[0]);
                                    } else if (st == 14) {
                                        //ModelBipedDBC.getMethod("renderHairsV2",float.class,String.class,float.class,int.class,int.class,int.class,int.class,renderPlayerJBRA).invoke(modelMain,0.0625F, "373852546750347428545480193462285654801934283647478050340147507467501848505072675018255250726750183760656580501822475071675018255050716750189730327158501802475071675018973225673850189765616160501820414547655019545654216550195754542165501920475027655019943669346576193161503065231900475030655019406534276538199465393460501997654138655019976345453950189760494941501897615252415018976354563850189763494736501897614949395018976152523950189763525234501897584749395018976150493850189760545234501897585250415018885445474550189754475041501897545250435018885454523950185143607861501897415874585018514369196150185147768078391865525680565018974356806150188843567861501868396374615018975056805650189750568056501885582374615018975823726150187149568054501877495680565018774950785650189163236961501820", 0.0F, 0, 0, pl, race, playerRenderer);
                                    } else {
                                        //ModelBipedDBC.getMethod("renderHairsV2",float.class,String.class,float.class,int.class,int.class,int.class,int.class,renderPlayerJBRA).invoke(modelMain,0.0625F, dnsH, 0.0F, st, rg, pl, race, playerRenderer);
                                    }
                                } else if (hairback == 10) {
                                    //GL11.glColor3f(h1 + (float)renderPlayerJBRA.getField("r2").get(null), h1 + (float)renderPlayerJBRA.getField("g2").get(null), h1 + (float)renderPlayerJBRA.getField("b2").get(null));
                                    //ModelBipedDBC.getMethod("renderHeadwear",float.class).invoke(modelMain,0.0625F);
                                } else if (dhhwho) {
                                    if (st == 14) {
                                        //ModelBipedDBC.getMethod("renderHairsV2",float.class,String.class,float.class,int.class,int.class,int.class,int.class,renderPlayerJBRA).invoke(modelMain,0.0625F, "373852546750347428545480193462285654801934283647478050340147507467501848505072675018255250726750183760656580501822475071675018255050716750189730327158501802475071675018973225673850189765616160501820414547655019545654216550195754542165501920475027655019943669346576193161503065231900475030655019406534276538199465393460501997654138655019976345453950189760494941501897615252415018976354563850189763494736501897614949395018976152523950189763525234501897584749395018976150493850189760545234501897585250415018885445474550189754475041501897545250435018885454523950185143607861501897415874585018514369196150185147768078391865525680565018974356806150188843567861501868396374615018975056805650189750568056501885582374615018975823726150187149568054501877495680565018774950785650189163236961501820", 0.0F, 0, 0, pl, race, playerRenderer);
                                    } else {
                                        //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"" + ((String[])JRMCoreH.getField("HairsT").get(null))[st] + ((String[])JRMCoreH.getField("Hairs").get(null))[hairback]);
                                    }
                                }

                                if (st == 14) {
                                    tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                                    tailCol = (Boolean) JRMCoreH.getMethod("func_26n6").invoke(null) ? 13292516 : tailCol;
                                    int jx = skintype == 1 ? bodyc1 : tailCol;
                                    if ((Boolean) JRMCoreH.getMethod("rSai",int.class).invoke(null,race) && jx == 6498048 && st == 14) {
                                        if ((Boolean) JRMCoreH.getMethod("func_26n6").invoke(null)) {
                                            jx = 13292516;
                                        } else {
                                            jx = 14292268;
                                        }
                                    }

                                    //renderPlayerJBRA.getMethod("glColor3f",int.class).invoke(null,jx);
                                    //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                                }
                            }
                        }
                    }

                    /*if (mj) {
                        //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                        ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                    }*/

                    String[] stri;
                    if (dbc) {
                        stri = new String[]{"wshell", "whandleg"};
                        if (weight >= 0 && weight < stri.length) {
                            String[] wloc = new String[]{"roshiShell", "weightBands"};
                            //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                            //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,stri[weight]);
                        }
                    }

                    if ((Boolean) JGConfigClientSettings.getField("CLIENT_DA19").get(null) && ((Boolean) JRMCoreH.getMethod("DBC").invoke(null) || (Boolean) JRMCoreH.getMethod("NC").invoke(null))) {
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glDisable(2896);
                        GL11.glBlendFunc(770, 771);
                        GL11.glAlphaFunc(516, 0.003921569F);
                        GL11.glDepthMask(false);
                        int[] PlyrAttrbts = new int[((int[])(JRMCoreH.getField("PlyrAttrbts").get(null))).length];
                        stri = ((String[])(JRMCoreH.getField("dat14").get(null)))[pl].split(",");

                        for(maxBody = 0; maxBody < PlyrAttrbts.length; ++maxBody) {
                            PlyrAttrbts[maxBody] = Integer.parseInt(stri[maxBody]);
                        }

                        maxBody = (int) JRMCoreH.getMethod("stat",Entity.class, int.class, int.class, int.class, int.class, int.class, int.class, float.class).invoke(null,player, 2, powerType, 2, PlyrAttrbts[2], race, classID, 0.0F);

                        int curBody = Integer.parseInt((String)JRMCoreH.getMethod("data",String.class,int.class,String.class).invoke(null,player.getCommandSenderName(), 8, "200"));
                        float one = (float)maxBody / 100.0F;
                        int perc = (int)((float)curBody / one);
                        if (perc < 70) {
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        }

                        if (perc < 55) {
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        }

                        if (perc < 35) {
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        }

                        if (perc < 20) {
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            //ModelBipedDBC.getMethod("renderBody",float.class).invoke(modelMain,0.0625F);
                        }

                        GL11.glDepthMask(true);
                        GL11.glEnable(2896);
                        GL11.glDisable(3042);
                        GL11.glPopMatrix();
                        if ((Boolean) JRMCoreH.getMethod("NC").invoke(null) && powerType == 2) {
                            if (classID == 1 && stY > 0) {
                                //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                            } else if (classID == 2 && stY > 0) {
                                int doujutsuID = (stY - 1) / 3 + 1;
                                if (doujutsuID < 1) {
                                    doujutsuID = 1;
                                } else if (doujutsuID > 3) {
                                    doujutsuID = 3;
                                }

                                //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                                //ModelBipedDBC.getMethod("renderHairs",float.class,String.class).invoke(modelMain,0.0625F,"EYERIGHT");
                            }
                        }
                    }

                    GL11.glPopMatrix();
                    doit = false;
                    break;
                }
            }
        }

        /*if (((String[]) JRMCoreH.getField("plyrs").get(null)) != null && ((String[]) JRMCoreH.getField("plyrs").get(null)).length > 0 && !player.isInvisible() && (Boolean)JRMCoreH.getMethod("dnn",int.class).invoke(null,13)) {
            for(pl = 0; pl < ((String[]) JRMCoreH.getField("plyrs").get(null)).length; ++pl) {
                if (((String[]) JRMCoreH.getField("plyrs").get(null))[pl].equals(player.getCommandSenderName()) && (Boolean)JRMCoreH.getMethod("aliveState",int.class).invoke(null,pl)) {
                    GL11.glPushMatrix();
                    //GL11.glColor3f(1.0F + (float)renderPlayerJBRA.getField("r2").get(null), 1.0F + (float)renderPlayerJBRA.getField("g2").get(null), 1.0F + (float)renderPlayerJBRA.getField("b2").get(null));
                    ModelBipedDBC.getMethod("renderHalo",float.class).invoke(modelMain,0.0625F);
                    GL11.glPopMatrix();
                    break;
                }
            }
        }*/

        /*if (doit && !player.isInvisible() && player.func_152123_o()) {
            GL11.glPushMatrix();
            ModelBipedDBC.getMethod("renderHeadwear",float.class).invoke(modelMain,0.0625F);
            GL11.glPopMatrix();
        }*/
        model.bipedHead.offsetY -= headOffsetY;

        model.bipedLeftArm.offsetX -= armOffsetX;
        model.bipedRightArm.offsetX -= -armOffsetX;

        model.bipedLeftLeg.offsetX -= legOffsetX;
        model.bipedRightLeg.offsetX -= -legOffsetX;
        model.bipedLeftLeg.offsetY -= legOffsetY;
        model.bipedRightLeg.offsetY -= legOffsetY;
        model.bipedLeftLeg.offsetZ -= legOffsetZ;
        model.bipedRightLeg.offsetZ -= legOffsetZ;
    }
}
