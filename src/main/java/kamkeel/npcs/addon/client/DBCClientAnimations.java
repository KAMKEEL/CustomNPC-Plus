package kamkeel.npcs.addon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.AnimationMixinFunctions;
import noppes.npcs.client.ClientEventHandler;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DBCClientAnimations {

    public static void doDBCRender(EntityPlayer par1EntityPlayer) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Minecraft mc = Minecraft.getMinecraft();

        Class<?> JBRAH = Class.forName("JinRyuu.JBRA.JBRAH");
        Class<?> JRMCoreHDBC = Class.forName("JinRyuu.JRMCore.JRMCoreHDBC");
        Class<?> JRMCoreH = Class.forName("JinRyuu.JRMCore.JRMCoreH");
        Class<?> ExtendedPlayer = Class.forName("JinRyuu.JRMCore.i.ExtendedPlayer");
        Class<?> JGConfigClientSettings = Class.forName("JinRyuu.JRMCore.client.config.jrmc.JGConfigClientSettings");
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Class<?> JGConfigRaces = Class.forName("JinRyuu.JRMCore.server.config.dbc.JGConfigRaces");

        Method func_aam = RenderPlayerJBRA.getDeclaredMethod("func_aam", int.class, boolean.class, boolean.class);
        func_aam.setAccessible(true);

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
            if (!(boolean) JGConfigClientSettings.getField("CLIENT_DA4").get(null)) {
                id = -1;
            }

            int tailCol;

            int jx;
            int j;
            if (dbcBool) {
                String[] s = ((String) JRMCoreH.getMethod("data", String.class, int.class, String.class).invoke(null, acp.getCommandSenderName(), 1, "0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0")).split(";");
                tailCol = Integer.parseInt(s[2]);
                if (tailCol == 1) {
                    String[] PlyrSkills = (String[]) JRMCoreH.getMethod("PlyrSkills", EntityPlayer.class).invoke(null, acp);
                    jx = (int) JRMCoreH.getMethod("SklLvl", int.class, String[].class).invoke(null, 12, PlyrSkills);
                    j = (int) JRMCoreH.getMethod("SklLvl", int.class, String[].class).invoke(null, 15, PlyrSkills);
                    String ss = s[17];
                    boolean v = dbcBool && !ss.equals("-1");
                    GL11.glPushMatrix();
                    if (v && (jx > 0 || j > 0)) {
                        if (id > -1) {
                            func_aam.invoke(renderPlayer, id, false, true);
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
                AnimationMixinFunctions.renderLimbs();

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
                    AnimationMixinFunctions.renderLimbs();
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
                        AnimationMixinFunctions.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
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
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/1nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/2nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/3nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();
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
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/1A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/2A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/3A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc3);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/4A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();
            } else {
                if (saiOozar) {
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru1.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(skintype != 0 ? bodycm : 11374471);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();

                    tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                    jx = State != 0 && State != 7 ? (lg ? 10092390 : 16574610) : (skintype == 1 ? bodyc1 : tailCol);
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru2.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(jx);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();
                } else if (skintype != 0) {
                    bdyskn = new ResourceLocation("jinryuumodscore:cc/" + (gen == 1 ? "f" : "") + "hum.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(bodycm);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();
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
                        AnimationMixinFunctions.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
                    }

                    if (State == 14) {
                        tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                        tailCol = (boolean) JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null) ? 13292516 : tailCol;
                        tailCol = skintype == 1 ? bodyc1 : tailCol;
                        if ((boolean) JRMCoreH.getMethod("rSai", int.class).invoke(null, race) && tailCol == 6498048 && State == 14) {
                            if ((boolean) JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null)) {
                                tailCol = 13292516;
                            } else {
                                tailCol = 14292268;
                            }
                        }

                        mc.getTextureManager().bindTexture(new ResourceLocation("jinryuudragonbc:cc/ss4" + (skintype == 0 ? "a" : "b") + ".png"));
                        glColor3f(tailCol);
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
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
                float one = (float) tailCol / 100.0F;
                j = (int) ((float) tailCol / one);
                if (j < 70) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises1.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (j < 55) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises2.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (j < 35) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises3.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (j < 20) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises4.png"));
                    AnimationMixinFunctions.renderLimbs();
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
                    itemarmor = (ItemArmor) item;
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
                        func_aam.invoke(renderPlayer, id, false, true);
                    }

                    GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                    GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                    if (dbcarmor != null) {
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        modelMain.textureHeight = 64;
                        modelMain.textureWidth = 128;
                        AnimationMixinFunctions.renderLimbs();
                    } else {
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
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
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/1nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/2nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/nam/3nam" + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();
            } else if (race == 4 && dbcBool) {
                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/0A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodycm);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/1A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc1);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/2A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc2);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/3A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                glColor3f(bodyc3);
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();

                bdyskn = new ResourceLocation("jinryuudragonbc:cc/arc/" + (gen == 1 ? "f" : "m") + "/4A" + ((short[]) JRMCoreH.getField("TransFrSkn").get(null))[State] + plyrSpc + ".png");
                mc.getTextureManager().bindTexture(bdyskn);
                GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                AnimationMixinFunctions.renderLimbs();
            } else {
                if (saiOozar) {
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru1.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(skintype != 0 ? bodycm : 11374471);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();

                    jx = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                    j = State != 0 && State != 7 ? (lg ? 10092390 : 16574610) : (skintype == 1 ? bodyc1 : jx);
                    bdyskn = new ResourceLocation("jinryuudragonbc:cc/oozaru2.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(j);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();
                } else if (skintype != 0) {
                    bdyskn = new ResourceLocation("jinryuumodscore:cc/" + (gen == 1 ? "f" : "") + "hum.png");
                    mc.getTextureManager().bindTexture(bdyskn);
                    glColor3f(bodycm);
                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                    AnimationMixinFunctions.renderLimbs();
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
                        AnimationMixinFunctions.renderLimbs();
                    } else if (jhdsBool && (boolean) JBRAH.getMethod("getSkinHas", Object.class).invoke(null, data) && skintype == 0) {
                        GL11.glColor3f(h1 + getR(), h1 + getG(), h1 + getB());
                        mc.getTextureManager().bindTexture((ResourceLocation) JBRAH.getMethod("getSkinLoc", Object.class).invoke(null, data));

                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
                    }

                    if (State == 14) {
                        tailCol = race != 2 && bodytype == 0 ? 6498048 : bodytype;
                        tailCol = (boolean) JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null) ? 13292516 : tailCol;
                        jx = skintype == 1 ? bodyc1 : tailCol;
                        if ((boolean) JRMCoreH.getMethod("rSai", int.class).invoke(null, race) && jx == 6498048 && State == 14) {
                            if ((boolean) JRMCoreH.getMethod("isAprilFoolsModeOn").invoke(null)) {
                                jx = 13292516;
                            } else {
                                jx = 14292268;
                            }
                        }

                        mc.getTextureManager().bindTexture(new ResourceLocation("jinryuudragonbc:cc/ss4" + (skintype == 0 ? "a" : "b") + ".png"));
                        glColor3f(jx);
                        modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                        AnimationMixinFunctions.renderLimbs();
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
                    JRMCoreH.getField("Pwrtyp").get(null), 2,
                    ((int[]) JRMCoreH.getField("PlyrAttrbts").get(null))[2], race,
                    JRMCoreH.getField("Class").get(null), 0.0F);
                jx = Integer.parseInt((String) JRMCoreH.getMethod("data", String.class, int.class, String.class).invoke(null, par1EntityPlayer.getCommandSenderName(), 8, "200"));
                float one = (float) tailCol / 100.0F;
                int perc = (int) ((float) jx / one);
                if (perc < 70) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises1.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (perc < 55) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises2.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (perc < 35) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises3.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                if (perc < 20) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(new ResourceLocation("jinryuumodscore:cc/bruises4.png"));
                    AnimationMixinFunctions.renderLimbs();
                }

                GL11.glDepthMask(true);
                GL11.glEnable(2896);
                GL11.glDisable(3042);
                GL11.glPopMatrix();
            }

            if (itemstack != null) {
                item = itemstack.getItem();
                if (item instanceof ItemArmor) {
                    itemarmor = (ItemArmor) item;
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
                                func_aam.invoke(renderPlayer, id, false, true);
                                GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                                GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                                if (dbcarmor != null) {
                                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                    modelMain.textureHeight = 64;
                                    modelMain.textureWidth = 128;
                                    AnimationMixinFunctions.renderLimbs();
                                } else {
                                    modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                    AnimationMixinFunctions.renderLimbs();
                                }

                                GL11.glPopMatrix();
                            }
                        } else {
                            GL11.glPushMatrix();
                            func_aam.invoke(renderPlayer, id, false, true);
                            GL11.glColor3f(1.0F + getR(), 1.0F + getG(), 1.0F + getB());
                            GL11.glScalef(1.0001F, 1.0001F, 1.0001F);
                            if (dbcarmor != null) {
                                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                modelMain.textureHeight = 64;
                                modelMain.textureWidth = 128;
                                AnimationMixinFunctions.renderLimbs();
                            } else {
                                modelMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
                                AnimationMixinFunctions.renderLimbs();
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

    private static float getR() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getR");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private static float getB() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getB");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private static float getG() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        Method method = RenderPlayerJBRA.getDeclaredMethod("getG");
        method.setAccessible(true);
        return (float) method.invoke(null);
    }

    private static void glColor3f(int c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        float h2 = (float) (c >> 16 & 255) / 255.0F;
        float h3 = (float) (c >> 8 & 255) / 255.0F;
        float h4 = (float) (c & 255) / 255.0F;
        float h1 = 1.0F;
        float r = h1 * h2;
        float g = h1 * h3;
        float b = h1 * h4;
        GL11.glColor3f(r + getR(), g + getG(), b + getB());
    }
}
