package noppes.npcs.client.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import noppes.npcs.client.model.util.ModelPlaneRenderer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

import org.lwjgl.opengl.GL11;

public class ModelPony extends ModelBase
{

    private boolean rainboom;
    private float WingRotateAngleX;
    private float WingRotateAngleY;
    private float WingRotateAngleZ;
    private float TailRotateAngleY;
    public ModelRenderer Head;
    public ModelRenderer Headpiece[];
    public ModelRenderer Helmet;
    public ModelRenderer Body;
    public ModelPlaneRenderer Bodypiece[];
    public ModelRenderer RightArm;
    public ModelRenderer LeftArm;
    public ModelRenderer RightLeg;
    public ModelRenderer LeftLeg;
    public ModelRenderer unicornarm;
    public ModelPlaneRenderer Tail[];
    public ModelRenderer LeftWing[];
    public ModelRenderer RightWing[];
    public ModelRenderer LeftWingExt[];
    public ModelRenderer RightWingExt[];
    public boolean isPegasus;
    public boolean isUnicorn;
    public boolean isFlying;
    public boolean isGlow;
    public boolean isSleeping;
    public boolean isSneak;
    public boolean aimedBow;
    public int heldItemRight;

    public ModelPony(float f)
    {
    	init(f,0.0f);
    }

    public void init(float strech,float f)
    {
        float f2 = 0.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;
        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-4F, -4F, -6F, 8, 8, 8, strech);
        Head.setRotationPoint(f2, f3 + f, f4);
        Headpiece = new ModelRenderer[3];
        Headpiece[0] = new ModelRenderer(this, 12, 16);
        Headpiece[0].addBox(-4F, -6F, -1F, 2, 2, 2, strech);
        Headpiece[0].setRotationPoint(f2, f3 + f, f4);
        Headpiece[1] = new ModelRenderer(this, 12, 16);
        Headpiece[1].addBox(2.0F, -6F, -1F, 2, 2, 2, strech);
        Headpiece[1].setRotationPoint(f2, f3 + f, f4);
        Headpiece[2] = new ModelRenderer(this, 56, 0);
        Headpiece[2].addBox(-0.5F, -10F, -4F, 1, 4, 1, strech);
        Headpiece[2].setRotationPoint(f2, f3 + f, f4);
        Helmet = new ModelRenderer(this, 32, 0);
        Helmet.addBox(-4F, -4F, -6F, 8, 8, 8, strech + 0.5F);
        Helmet.setRotationPoint(f2, f3, f4);
        float f5 = 0.0F;
        float f6 = 0.0F;
        float f7 = 0.0F;
        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 4F, -2F, 8, 8, 4, strech);
        Body.setRotationPoint(f5, f6 + f, f7);
        Bodypiece = new ModelPlaneRenderer[13];
        Bodypiece[0] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[0].addSidePlane(-4F, 4F, 2.0F, 8, 8, strech);
        Bodypiece[0].setRotationPoint(f5, f6 + f, f7);
        Bodypiece[1] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[1].addSidePlane(4F, 4F, 2.0F, 8, 8, strech);
        Bodypiece[1].setRotationPoint(f5, f6 + f, f7);
        Bodypiece[2] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[2].addTopPlane(-4F, 4F, 2.0F, 8, 8, strech);
        Bodypiece[2].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[3] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[3].addTopPlane(-4F, 12F, 2.0F, 8, 8, strech);
        Bodypiece[3].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[4] = new ModelPlaneRenderer(this, 0, 20);
        Bodypiece[4].addSidePlane(-4F, 4F, 10F, 8, 4, strech);
        Bodypiece[4].setRotationPoint(f5, f6 + f, f7);
        Bodypiece[5] = new ModelPlaneRenderer(this, 0, 20);
        Bodypiece[5].addSidePlane(4F, 4F, 10F, 8, 4, strech);
        Bodypiece[5].setRotationPoint(f5, f6 + f, f7);
        Bodypiece[6] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[6].addTopPlane(-4F, 4F, 10F, 8, 4, strech);
        Bodypiece[6].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[7] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[7].addTopPlane(-4F, 12F, 10F, 8, 4, strech);
        Bodypiece[7].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[8] = new ModelPlaneRenderer(this, 24, 0);
        Bodypiece[8].addBackPlane(-4F, 4F, 14F, 8, 8, strech);
        Bodypiece[8].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[9] = new ModelPlaneRenderer(this, 32, 0);
        Bodypiece[9].addTopPlane(-1F, 10F, 8F, 2, 6, strech);
        Bodypiece[9].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[10] = new ModelPlaneRenderer(this, 32, 0);
        Bodypiece[10].addTopPlane(-1F, 12F, 8F, 2, 6, strech);
        Bodypiece[10].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[11] = new ModelPlaneRenderer(this, 32, 0);
        Bodypiece[11].mirror = true;
        Bodypiece[11].addSidePlane(-1F, 10F, 8F, 2, 6, strech);
        Bodypiece[11].setRotationPoint(f2, f3 + f, f4);
        Bodypiece[12] = new ModelPlaneRenderer(this, 32, 0);
        Bodypiece[12].addSidePlane(1.0F, 10F, 8F, 2, 6, strech);
        Bodypiece[12].setRotationPoint(f2, f3 + f, f4);
        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        RightArm.setRotationPoint(-3F, 8F + f, 0.0F);
        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.mirror = true;
        LeftArm.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        LeftArm.setRotationPoint(3F, 8F + f, 0.0F);
        RightLeg = new ModelRenderer(this, 40, 16);
        RightLeg.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        RightLeg.setRotationPoint(-3F, 0.0F + f, 0.0F);
        LeftLeg = new ModelRenderer(this, 40, 16);
        LeftLeg.mirror = true;
        LeftLeg.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        LeftLeg.setRotationPoint(3F, 0.0F + f, 0.0F);
        unicornarm = new ModelRenderer(this, 40, 16);
        unicornarm.addBox(-3F, -2F, -2F, 4, 12, 4, strech);
        unicornarm.setRotationPoint(-5F, 2.0F + f, 0.0F);
        float f8 = 0.0F;
        float f9 = 8F;
        float f10 = -14F;
        float f11 = 0.0F - f8;
        float f12 = 10F - f9;
        float f13 = 0.0F;
        Tail = new ModelPlaneRenderer[10];
        Tail[0] = new ModelPlaneRenderer(this, 32, 0);
        Tail[0].addTopPlane(-2F + f8, -7F + f9, 16F + f10, 4, 4, strech);
        Tail[0].setRotationPoint(f11, f12 + f, f13);
        Tail[1] = new ModelPlaneRenderer(this, 32, 0);
        Tail[1].addTopPlane(-2F + f8, 9F + f9, 16F + f10, 4, 4, strech);
        Tail[1].setRotationPoint(f11, f12 + f, f13);
        Tail[2] = new ModelPlaneRenderer(this, 32, 0);
        Tail[2].addBackPlane(-2F + f8, -7F + f9, 16F + f10, 4, 8, strech);
        Tail[2].setRotationPoint(f11, f12 + f, f13);
        Tail[3] = new ModelPlaneRenderer(this, 32, 0);
        Tail[3].addBackPlane(-2F + f8, -7F + f9, 20F + f10, 4, 8, strech);
        Tail[3].setRotationPoint(f11, f12 + f, f13);
        Tail[4] = new ModelPlaneRenderer(this, 32, 0);
        Tail[4].addBackPlane(-2F + f8, 1.0F + f9, 16F + f10, 4, 8, strech);
        Tail[4].setRotationPoint(f11, f12 + f, f13);
        Tail[5] = new ModelPlaneRenderer(this, 32, 0);
        Tail[5].addBackPlane(-2F + f8, 1.0F + f9, 20F + f10, 4, 8, strech);
        Tail[5].setRotationPoint(f11, f12 + f, f13);
        Tail[6] = new ModelPlaneRenderer(this, 36, 0);
        Tail[6].mirror = true;
        Tail[6].addSidePlane(2.0F + f8, -7F + f9, 16F + f10, 8, 4, strech);
        Tail[6].setRotationPoint(f11, f12 + f, f13);
        Tail[7] = new ModelPlaneRenderer(this, 36, 0);
        Tail[7].addSidePlane(-2F + f8, -7F + f9, 16F + f10, 8, 4, strech);
        Tail[7].setRotationPoint(f11, f12 + f, f13);
        Tail[8] = new ModelPlaneRenderer(this, 36, 0);
        Tail[8].mirror = true;
        Tail[8].addSidePlane(2.0F + f8, 1.0F + f9, 16F + f10, 8, 4, strech);
        Tail[8].setRotationPoint(f11, f12 + f, f13);
        Tail[9] = new ModelPlaneRenderer(this, 36, 0);
        Tail[9].addSidePlane(-2F + f8, 1.0F + f9, 16F + f10, 8, 4, strech);
        Tail[9].setRotationPoint(f11, f12 + f, f13);
        TailRotateAngleY = Tail[0].rotateAngleY;
        TailRotateAngleY = Tail[0].rotateAngleY;
        float f14 = 0.0F;
        float f15 = 0.0F;
        float f16 = 0.0F;
        LeftWing = new ModelRenderer[3];
        LeftWing[0] = new ModelRenderer(this, 56, 16);
        LeftWing[0].mirror = true;
        LeftWing[0].addBox(4F, 5F, 2.0F, 2, 6, 2, strech);
        LeftWing[0].setRotationPoint(f14, f15 + f, f16);
        LeftWing[1] = new ModelRenderer(this, 56, 16);
        LeftWing[1].mirror = true;
        LeftWing[1].addBox(4F, 5F, 4F, 2, 8, 2, strech);
        LeftWing[1].setRotationPoint(f14, f15 + f, f16);
        LeftWing[2] = new ModelRenderer(this, 56, 16);
        LeftWing[2].mirror = true;
        LeftWing[2].addBox(4F, 5F, 6F, 2, 6, 2, strech);
        LeftWing[2].setRotationPoint(f14, f15 + f, f16);
        RightWing = new ModelRenderer[3];
        RightWing[0] = new ModelRenderer(this, 56, 16);
        RightWing[0].addBox(-6F, 5F, 2.0F, 2, 6, 2, strech);
        RightWing[0].setRotationPoint(f14, f15 + f, f16);
        RightWing[1] = new ModelRenderer(this, 56, 16);
        RightWing[1].addBox(-6F, 5F, 4F, 2, 8, 2, strech);
        RightWing[1].setRotationPoint(f14, f15 + f, f16);
        RightWing[2] = new ModelRenderer(this, 56, 16);
        RightWing[2].addBox(-6F, 5F, 6F, 2, 6, 2, strech);
        RightWing[2].setRotationPoint(f14, f15 + f, f16);
        float f17 = f2 + 4.5F;
        float f18 = f3 + 5F;
        float f19 = f4 + 6F;
        LeftWingExt = new ModelRenderer[7];
        LeftWingExt[0] = new ModelRenderer(this, 56, 19);
        LeftWingExt[0].mirror = true;
        LeftWingExt[0].addBox(0.0F, 0.0F, 0.0F, 1, 8, 2, strech + 0.1F);
        LeftWingExt[0].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[1] = new ModelRenderer(this, 56, 19);
        LeftWingExt[1].mirror = true;
        LeftWingExt[1].addBox(0.0F, 8F, 0.0F, 1, 6, 2, strech + 0.1F);
        LeftWingExt[1].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[2] = new ModelRenderer(this, 56, 19);
        LeftWingExt[2].mirror = true;
        LeftWingExt[2].addBox(0.0F, -1.2F, -0.2F, 1, 8, 2, strech - 0.2F);
        LeftWingExt[2].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[3] = new ModelRenderer(this, 56, 19);
        LeftWingExt[3].mirror = true;
        LeftWingExt[3].addBox(0.0F, 1.8F, 1.3F, 1, 8, 2, strech - 0.1F);
        LeftWingExt[3].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[4] = new ModelRenderer(this, 56, 19);
        LeftWingExt[4].mirror = true;
        LeftWingExt[4].addBox(0.0F, 5F, 2.0F, 1, 8, 2, strech);
        LeftWingExt[4].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[5] = new ModelRenderer(this, 56, 19);
        LeftWingExt[5].mirror = true;
        LeftWingExt[5].addBox(0.0F, 0.0F, -0.2F, 1, 6, 2, strech + 0.3F);
        LeftWingExt[5].setRotationPoint(f17, f18 + f, f19);
        LeftWingExt[6] = new ModelRenderer(this, 56, 19);
        LeftWingExt[6].mirror = true;
        LeftWingExt[6].addBox(0.0F, 0.0F, 0.2F, 1, 3, 2, strech + 0.2F);
        LeftWingExt[6].setRotationPoint(f17, f18 + f, f19);
        float f20 = f2 - 4.5F;
        float f21 = f3 + 5F;
        float f22 = f4 + 6F;
        RightWingExt = new ModelRenderer[7];
        RightWingExt[0] = new ModelRenderer(this, 56, 19);
        RightWingExt[0].mirror = true;
        RightWingExt[0].addBox(0.0F, 0.0F, 0.0F, 1, 8, 2, strech + 0.1F);
        RightWingExt[0].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[1] = new ModelRenderer(this, 56, 19);
        RightWingExt[1].mirror = true;
        RightWingExt[1].addBox(0.0F, 8F, 0.0F, 1, 6, 2, strech + 0.1F);
        RightWingExt[1].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[2] = new ModelRenderer(this, 56, 19);
        RightWingExt[2].mirror = true;
        RightWingExt[2].addBox(0.0F, -1.2F, -0.2F, 1, 8, 2, strech - 0.2F);
        RightWingExt[2].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[3] = new ModelRenderer(this, 56, 19);
        RightWingExt[3].mirror = true;
        RightWingExt[3].addBox(0.0F, 1.8F, 1.3F, 1, 8, 2, strech - 0.1F);
        RightWingExt[3].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[4] = new ModelRenderer(this, 56, 19);
        RightWingExt[4].mirror = true;
        RightWingExt[4].addBox(0.0F, 5F, 2.0F, 1, 8, 2, strech);
        RightWingExt[4].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[5] = new ModelRenderer(this, 56, 19);
        RightWingExt[5].mirror = true;
        RightWingExt[5].addBox(0.0F, 0.0F, -0.2F, 1, 6, 2, strech + 0.3F);
        RightWingExt[5].setRotationPoint(f20, f21 + f, f22);
        RightWingExt[6] = new ModelRenderer(this, 56, 19);
        RightWingExt[6].mirror = true;
        RightWingExt[6].addBox(0.0F, 0.0F, 0.2F, 1, 3, 2, strech + 0.2F);
        RightWingExt[6].setRotationPoint(f20, f21 + f, f22);
        WingRotateAngleX = LeftWingExt[0].rotateAngleX;
        WingRotateAngleY = LeftWingExt[0].rotateAngleY;
        WingRotateAngleZ = LeftWingExt[0].rotateAngleZ;
    }


    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
    	EntityNPCInterface npc = (EntityNPCInterface) entity;
    	isRiding = npc.isRiding();
    	
    	if(isSneak && (npc.currentAnimation == EnumAnimation.CRAWLING || npc.currentAnimation == EnumAnimation.LYING))
    		isSneak = false;
        rainboom = false;
        float f6;
        float f7;
        if(isSleeping)
        {
            f6 = 1.4F;
            f7 = 0.1F;
        } else
        {
            f6 = f3 / 57.29578F;
            f7 = f4 / 57.29578F;
        }
        Head.rotateAngleY = f6;
        Head.rotateAngleX = f7;
        Headpiece[0].rotateAngleY = f6;
        Headpiece[0].rotateAngleX = f7;
        Headpiece[1].rotateAngleY = f6;
        Headpiece[1].rotateAngleX = f7;
        Headpiece[2].rotateAngleY = f6;
        Headpiece[2].rotateAngleX = f7;
        Helmet.rotateAngleY = f6;
        Helmet.rotateAngleX = f7;
        Headpiece[2].rotateAngleX = f7 + 0.5F;
        float f8;
        float f9;
        float f10;
        float f11;
        if(!isFlying || !isPegasus)
        {
            f8 = MathHelper.cos(f * 0.6662F + 3.141593F) * 0.6F * f1;
            f9 = MathHelper.cos(f * 0.6662F) * 0.6F * f1;
            f10 = MathHelper.cos(f * 0.6662F) * 0.3F * f1;
            f11 = MathHelper.cos(f * 0.6662F + 3.141593F) * 0.3F * f1;
            RightArm.rotateAngleY = 0.0F;
            unicornarm.rotateAngleY = 0.0F;
            LeftArm.rotateAngleY = 0.0F;
            RightLeg.rotateAngleY = 0.0F;
            LeftLeg.rotateAngleY = 0.0F;
        } else
        {
            if(f1 < 0.9999F)
            {
                rainboom = false;
                f8 = MathHelper.sin(0.0F - f1 * 0.5F);
                f9 = MathHelper.sin(0.0F - f1 * 0.5F);
                f10 = MathHelper.sin(f1 * 0.5F);
                f11 = MathHelper.sin(f1 * 0.5F);
            } else
            {
                rainboom = true;
                f8 = 4.712F;
                f9 = 4.712F;
                f10 = 1.571F;
                f11 = 1.571F;
            }
            RightArm.rotateAngleY = 0.2F;
            LeftArm.rotateAngleY = -0.2F;
            RightLeg.rotateAngleY = -0.2F;
            LeftLeg.rotateAngleY = 0.2F;
        }
        if(isSleeping)
        {
            f8 = 4.712F;
            f9 = 4.712F;
            f10 = 1.571F;
            f11 = 1.571F;
        }
        RightArm.rotateAngleX = f8;
        unicornarm.rotateAngleX = 0.0F;
        LeftArm.rotateAngleX = f9;
        RightLeg.rotateAngleX = f10;
        LeftLeg.rotateAngleX = f11;
        RightArm.rotateAngleZ = 0.0F;
        unicornarm.rotateAngleZ = 0.0F;
        LeftArm.rotateAngleZ = 0.0F;
        for(int i = 0; i < Tail.length; i++)
        {
            if(rainboom)
            {
                Tail[i].rotateAngleZ = 0.0F;
            } else
            {
                Tail[i].rotateAngleZ = MathHelper.cos(f * 0.8F) * 0.2F * f1;
            }
        }

        if(heldItemRight != 0 && !rainboom && !isUnicorn)
        {
            RightArm.rotateAngleX = RightArm.rotateAngleX * 0.5F - 0.3141593F;
        }
        float f12 = 0.0F;
        if(f5 > -9990F && !isUnicorn)
        {
            f12 = MathHelper.sin(MathHelper.sqrt_float(f5) * 3.141593F * 2.0F) * 0.2F;
        }
        Body.rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        for(int j = 0; j < Bodypiece.length; j++)
        {
            Bodypiece[j].rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        }

        for(int k = 0; k < LeftWing.length; k++)
        {
            LeftWing[k].rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        }

        for(int l = 0; l < RightWing.length; l++)
        {
            RightWing[l].rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        }

        for(int i1 = 0; i1 < Tail.length; i1++)
        {
            Tail[i1].rotateAngleY = f12;
        }

        float f13 = MathHelper.sin(Body.rotateAngleY) * 5F;
        float f14 = MathHelper.cos(Body.rotateAngleY) * 5F;
        float f15 = 4F;
        if(isSneak && !isFlying)
        {
            f15 = 0.0F;
        }
        if(isSleeping)
        {
            f15 = 2.6F;
        }
        if(rainboom)
        {
            RightArm.rotationPointZ = f13 + 2.0F;
            LeftArm.rotationPointZ = (0.0F - f13) + 2.0F;
        } else
        {
            RightArm.rotationPointZ = f13 + 1.0F;
            LeftArm.rotationPointZ = (0.0F - f13) + 1.0F;
        }
        RightArm.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        LeftArm.rotationPointX = (f14 + 1.0F) - f15;
        RightLeg.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        LeftLeg.rotationPointX = (f14 + 1.0F) - f15;
        RightArm.rotateAngleY += Body.rotateAngleY;
        LeftArm.rotateAngleY += Body.rotateAngleY;
        LeftArm.rotateAngleX += Body.rotateAngleY;
        RightArm.rotationPointY = 8F;
        LeftArm.rotationPointY = 8F;
        RightLeg.rotationPointY = 4F;
        LeftLeg.rotationPointY = 4F;
        if(f5 > -9990F)
        {
            float f16 = f5;
            f16 = 1.0F - f5;
            f16 *= f16 * f16;
            f16 = 1.0F - f16;
            float f22 = MathHelper.sin(f16 * 3.141593F);
            float f28 = MathHelper.sin(f5 * 3.141593F);
            float f33 = f28 * -(Head.rotateAngleX - 0.7F) * 0.75F;
            if(isUnicorn)
            {
                unicornarm.rotateAngleX -= (double)f22 * 1.2D + (double)f33;
                unicornarm.rotateAngleY += Body.rotateAngleY * 2.0F;
                unicornarm.rotateAngleZ = f28 * -0.4F;
            } else
            {
                unicornarm.rotateAngleX -= (double)f22 * 1.2D + (double)f33;
                unicornarm.rotateAngleY += Body.rotateAngleY * 2.0F;
                unicornarm.rotateAngleZ = f28 * -0.4F;
//                RightArm.rotateAngleX -= (double)f22 * 1.2D + (double)f33;
//                RightArm.rotateAngleY += Body.rotateAngleY * 2.0F;
//                RightArm.rotateAngleZ = f28 * -0.4F;
            }
        }
        if(isSneak && !isFlying)
        {
            float f17 = 0.4F;
            float f23 = 7F;
            float f29 = -4F;
            Body.rotateAngleX = f17;
            Body.rotationPointY = f23;
            Body.rotationPointZ = f29;
            for(int i3 = 0; i3 < Bodypiece.length; i3++)
            {
                Bodypiece[i3].rotateAngleX = f17;
                Bodypiece[i3].rotationPointY = f23;
                Bodypiece[i3].rotationPointZ = f29;
            }

            float f34 = 3.5F;
            float f37 = 6F;
            for(int i4 = 0; i4 < LeftWingExt.length; i4++)
            {
                LeftWingExt[i4].rotateAngleX = (float)((double)f17 + 2.3561947345733643D);
                LeftWingExt[i4].rotationPointY = f23 + f34;
                LeftWingExt[i4].rotationPointZ = f29 + f37;
                LeftWingExt[i4].rotateAngleX = 2.5F;
                LeftWingExt[i4].rotateAngleZ = -6F;
            }

            float f40 = 4.5F;
            float f43 = 6F;
            for(int i5 = 0; i5 < LeftWingExt.length; i5++)
            {
                RightWingExt[i5].rotateAngleX = (float)((double)f17 + 2.3561947345733643D);
                RightWingExt[i5].rotationPointY = f23 + f40;
                RightWingExt[i5].rotationPointZ = f29 + f43;
                RightWingExt[i5].rotateAngleX = 2.5F;
                RightWingExt[i5].rotateAngleZ = 6F;
            }

            RightLeg.rotateAngleX -= 0.0F;
            LeftLeg.rotateAngleX -= 0.0F;
            RightArm.rotateAngleX -= 0.4F;
            unicornarm.rotateAngleX += 0.4F;
            LeftArm.rotateAngleX -= 0.4F;
            RightLeg.rotationPointZ = 10F;
            LeftLeg.rotationPointZ = 10F;
            RightLeg.rotationPointY = 7F;
            LeftLeg.rotationPointY = 7F;
            float f46;
            float f48;
            float f50;
            if(isSleeping)
            {
                f46 = 2.0F;
                f48 = -1F;
                f50 = 1.0F;
            } else
            {
                f46 = 6F;
                f48 = -2F;
                f50 = 0.0F;
            }
            Head.rotationPointY = f46;
            Head.rotationPointZ = f48;
            Head.rotationPointX = f50;
            Helmet.rotationPointY = f46;
            Helmet.rotationPointZ = f48;
            Helmet.rotationPointX = f50;
            Headpiece[0].rotationPointY = f46;
            Headpiece[0].rotationPointZ = f48;
            Headpiece[0].rotationPointX = f50;
            Headpiece[1].rotationPointY = f46;
            Headpiece[1].rotationPointZ = f48;
            Headpiece[1].rotationPointX = f50;
            Headpiece[2].rotationPointY = f46;
            Headpiece[2].rotationPointZ = f48;
            Headpiece[2].rotationPointX = f50;
            float f52 = 0.0F;
            float f54 = 8F;
            float f56 = -14F;
            float f58 = 0.0F - f52;
            float f60 = 9F - f54;
            float f62 = -4F - f56;
            float f63 = 0.0F;
            for(int i6 = 0; i6 < Tail.length; i6++)
            {
                Tail[i6].rotationPointX = f58;
                Tail[i6].rotationPointY = f60;
                Tail[i6].rotationPointZ = f62;
                Tail[i6].rotateAngleX = f63;
            }

        } else
        {
            float f18 = 0.0F;
            float f24 = 0.0F;
            float f30 = 0.0F;
            Body.rotateAngleX = f18;
            Body.rotationPointY = f24;
            Body.rotationPointZ = f30;
            for(int j3 = 0; j3 < Bodypiece.length; j3++)
            {
                Bodypiece[j3].rotateAngleX = f18;
                Bodypiece[j3].rotationPointY = f24;
                Bodypiece[j3].rotationPointZ = f30;
            }

            if(isPegasus)
            {
                if(!isFlying)
                {
                    for(int k3 = 0; k3 < LeftWing.length; k3++)
                    {
                        LeftWing[k3].rotateAngleX = (float)((double)f18 + 1.5707964897155762D);
                        LeftWing[k3].rotationPointY = f24 + 13F;
                        LeftWing[k3].rotationPointZ = f30 - 3F;
                    }

                    for(int l3 = 0; l3 < RightWing.length; l3++)
                    {
                        RightWing[l3].rotateAngleX = (float)((double)f18 + 1.5707964897155762D);
                        RightWing[l3].rotationPointY = f24 + 13F;
                        RightWing[l3].rotationPointZ = f30 - 3F;
                    }

                } else
                {
                    float f35 = 5.5F;
                    float f38 = 3F;
                    for(int j4 = 0; j4 < LeftWingExt.length; j4++)
                    {
                        LeftWingExt[j4].rotateAngleX = (float)((double)f18 + 1.5707964897155762D);
                        LeftWingExt[j4].rotationPointY = f24 + f35;
                        LeftWingExt[j4].rotationPointZ = f30 + f38;
                    }

                    float f41 = 6.5F;
                    float f44 = 3F;
                    for(int j5 = 0; j5 < RightWingExt.length; j5++)
                    {
                        RightWingExt[j5].rotateAngleX = (float)((double)f18 + 1.5707964897155762D);
                        RightWingExt[j5].rotationPointY = f24 + f41;
                        RightWingExt[j5].rotationPointZ = f30 + f44;
                    }

                }
            }
            RightLeg.rotationPointZ = 10F;
            LeftLeg.rotationPointZ = 10F;
            RightLeg.rotationPointY = 8F;
            LeftLeg.rotationPointY = 8F;
            float f36 = MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
            float f39 = MathHelper.sin(f2 * 0.067F) * 0.05F;
            unicornarm.rotateAngleZ += f36;
            unicornarm.rotateAngleX += f39;
            if(isPegasus && isFlying)
            {
                WingRotateAngleY = MathHelper.sin(f2 * 0.067F * 8F) * 1.0F;
                WingRotateAngleZ = MathHelper.sin(f2 * 0.067F * 8F) * 1.0F;
                for(int k4 = 0; k4 < LeftWingExt.length; k4++)
                {
                    LeftWingExt[k4].rotateAngleX = 2.5F;
                    LeftWingExt[k4].rotateAngleZ = -WingRotateAngleZ - 4.712F - 0.4F;
                }

                for(int l4 = 0; l4 < RightWingExt.length; l4++)
                {
                    RightWingExt[l4].rotateAngleX = 2.5F;
                    RightWingExt[l4].rotateAngleZ = WingRotateAngleZ + 4.712F + 0.4F;
                }

            }
            float f42;
            float f45;
            float f47;
            if(isSleeping)
            {
                f42 = 2.0F;
                f45 = 1.0F;
                f47 = 1.0F;
            } else
            {
                f42 = 0.0F;
                f45 = 0.0F;
                f47 = 0.0F;
            }
            Head.rotationPointY = f42;
            Head.rotationPointZ = f45;
            Head.rotationPointX = f47;
            Helmet.rotationPointY = f42;
            Helmet.rotationPointZ = f45;
            Helmet.rotationPointX = f47;
            Headpiece[0].rotationPointY = f42;
            Headpiece[0].rotationPointZ = f45;
            Headpiece[0].rotationPointX = f47;
            Headpiece[1].rotationPointY = f42;
            Headpiece[1].rotationPointZ = f45;
            Headpiece[1].rotationPointX = f47;
            Headpiece[2].rotationPointY = f42;
            Headpiece[2].rotationPointZ = f45;
            Headpiece[2].rotationPointX = f47;
            float f49 = 0.0F;
            float f51 = 8F;
            float f53 = -14F;
            float f55 = 0.0F - f49;
            float f57 = 9F - f51;
            float f59 = 0.0F - f53;
            float f61 = 0.5F * f1;
            for(int k5 = 0; k5 < Tail.length; k5++)
            {
                Tail[k5].rotationPointX = f55;
                Tail[k5].rotationPointY = f57;
                Tail[k5].rotationPointZ = f59;
                if(rainboom)
                {
                    Tail[k5].rotateAngleX = 1.571F + 0.1F * MathHelper.sin(f);
                } else
                {
                    Tail[k5].rotateAngleX = f61;
                }
            }

            for(int l5 = 0; l5 < Tail.length; l5++)
            {
                if(!rainboom)
                {
                    Tail[l5].rotateAngleX += f39;
                }
            }

        }
        LeftWingExt[2].rotateAngleX = LeftWingExt[2].rotateAngleX - 0.85F;
        LeftWingExt[3].rotateAngleX = LeftWingExt[3].rotateAngleX - 0.75F;
        LeftWingExt[4].rotateAngleX = LeftWingExt[4].rotateAngleX - 0.5F;
        LeftWingExt[6].rotateAngleX = LeftWingExt[6].rotateAngleX - 0.85F;
        RightWingExt[2].rotateAngleX = RightWingExt[2].rotateAngleX - 0.85F;
        RightWingExt[3].rotateAngleX = RightWingExt[3].rotateAngleX - 0.75F;
        RightWingExt[4].rotateAngleX = RightWingExt[4].rotateAngleX - 0.5F;
        RightWingExt[6].rotateAngleX = RightWingExt[6].rotateAngleX - 0.85F;
        Bodypiece[9].rotateAngleX = Bodypiece[9].rotateAngleX + 0.5F;
        Bodypiece[10].rotateAngleX = Bodypiece[10].rotateAngleX + 0.5F;
        Bodypiece[11].rotateAngleX = Bodypiece[11].rotateAngleX + 0.5F;
        Bodypiece[12].rotateAngleX = Bodypiece[12].rotateAngleX + 0.5F;
        if(rainboom)
        {
            for(int j1 = 0; j1 < Tail.length; j1++)
            {
                Tail[j1].rotationPointY = Tail[j1].rotationPointY + 6F;
                Tail[j1].rotationPointZ = Tail[j1].rotationPointZ + 1.0F;
            }

        }
//        if(isRiding)
//        {
//            float f19 = -10F;
//            float f25 = -10F;
//            Head.rotationPointY = Head.rotationPointY + f19;
//            Head.rotationPointZ = Head.rotationPointZ + f25;
//            Headpiece[0].rotationPointY = Headpiece[0].rotationPointY + f19;
//            Headpiece[0].rotationPointZ = Headpiece[0].rotationPointZ + f25;
//            Headpiece[1].rotationPointY = Headpiece[1].rotationPointY + f19;
//            Headpiece[1].rotationPointZ = Headpiece[1].rotationPointZ + f25;
//            Headpiece[2].rotationPointY = Headpiece[2].rotationPointY + f19;
//            Headpiece[2].rotationPointZ = Headpiece[2].rotationPointZ + f25;
//            Helmet.rotationPointY = Helmet.rotationPointY + f19;
//            Helmet.rotationPointZ = Helmet.rotationPointZ + f25;
//            Body.rotationPointY = Body.rotationPointY + f19;
//            Body.rotationPointZ = Body.rotationPointZ + f25;
//            for(int k1 = 0; k1 < Bodypiece.length; k1++)
//            {
//                Bodypiece[k1].rotationPointY = Bodypiece[k1].rotationPointY + f19;
//                Bodypiece[k1].rotationPointZ = Bodypiece[k1].rotationPointZ + f25;
//            }
//
//            LeftArm.rotationPointY = LeftArm.rotationPointY + f19;
//            LeftArm.rotationPointZ = LeftArm.rotationPointZ + f25;
//            RightArm.rotationPointY = RightArm.rotationPointY + f19;
//            RightArm.rotationPointZ = RightArm.rotationPointZ + f25;
//            LeftLeg.rotationPointY = LeftLeg.rotationPointY + f19;
//            LeftLeg.rotationPointZ = LeftLeg.rotationPointZ + f25;
//            RightLeg.rotationPointY = RightLeg.rotationPointY + f19;
//            RightLeg.rotationPointZ = RightLeg.rotationPointZ + f25;
//            for(int l1 = 0; l1 < Tail.length; l1++)
//            {
//                Tail[l1].rotationPointY = Tail[l1].rotationPointY + f19;
//                Tail[l1].rotationPointZ = Tail[l1].rotationPointZ + f25;
//            }
//
//            for(int i2 = 0; i2 < LeftWing.length; i2++)
//            {
//                LeftWing[i2].rotationPointY = LeftWing[i2].rotationPointY + f19;
//                LeftWing[i2].rotationPointZ = LeftWing[i2].rotationPointZ + f25;
//            }
//
//            for(int j2 = 0; j2 < RightWing.length; j2++)
//            {
//                RightWing[j2].rotationPointY = RightWing[j2].rotationPointY + f19;
//                RightWing[j2].rotationPointZ = RightWing[j2].rotationPointZ + f25;
//            }
//
//            for(int k2 = 0; k2 < LeftWingExt.length; k2++)
//            {
//                LeftWingExt[k2].rotationPointY = LeftWingExt[k2].rotationPointY + f19;
//                LeftWingExt[k2].rotationPointZ = LeftWingExt[k2].rotationPointZ + f25;
//            }
//
//            for(int l2 = 0; l2 < RightWingExt.length; l2++)
//            {
//                RightWingExt[l2].rotationPointY = RightWingExt[l2].rotationPointY + f19;
//                RightWingExt[l2].rotationPointZ = RightWingExt[l2].rotationPointZ + f25;
//            }
//
//        }
        if(isSleeping)
        {
            RightArm.rotationPointZ = RightArm.rotationPointZ + 6F;
            LeftArm.rotationPointZ = LeftArm.rotationPointZ + 6F;
            RightLeg.rotationPointZ = RightLeg.rotationPointZ - 8F;
            LeftLeg.rotationPointZ = LeftLeg.rotationPointZ - 8F;
            RightArm.rotationPointY = RightArm.rotationPointY + 2.0F;
            LeftArm.rotationPointY = LeftArm.rotationPointY + 2.0F;
            RightLeg.rotationPointY = RightLeg.rotationPointY + 2.0F;
            LeftLeg.rotationPointY = LeftLeg.rotationPointY + 2.0F;
        }
        if(aimedBow)
        {
            if(isUnicorn)
            {
                float f20 = 0.0F;
                float f26 = 0.0F;
                unicornarm.rotateAngleZ = 0.0F;
                unicornarm.rotateAngleY = -(0.1F - f20 * 0.6F) + Head.rotateAngleY;
                unicornarm.rotateAngleX = 4.712F + Head.rotateAngleX;
                unicornarm.rotateAngleX -= f20 * 1.2F - f26 * 0.4F;
                float f31 = f2;
                unicornarm.rotateAngleZ += MathHelper.cos(f31 * 0.09F) * 0.05F + 0.05F;
                unicornarm.rotateAngleX += MathHelper.sin(f31 * 0.067F) * 0.05F;
            } else
            {
                float f21 = 0.0F;
                float f27 = 0.0F;
                RightArm.rotateAngleZ = 0.0F;
                RightArm.rotateAngleY = -(0.1F - f21 * 0.6F) + Head.rotateAngleY;
                RightArm.rotateAngleX = 4.712F + Head.rotateAngleX;
                RightArm.rotateAngleX -= f21 * 1.2F - f27 * 0.4F;
                float f32 = f2;
                RightArm.rotateAngleZ += MathHelper.cos(f32 * 0.09F) * 0.05F + 0.05F;
                RightArm.rotateAngleX += MathHelper.sin(f32 * 0.067F) * 0.05F;
                RightArm.rotationPointZ = RightArm.rotationPointZ + 1.0F;
            }
        }
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
    	EntityNpcPony pony = (EntityNpcPony) entity;
    	if(pony.textureLocation != pony.checked && pony.textureLocation != null){
    		try {
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(pony.textureLocation);
				BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());

				pony.isPegasus = false;
				pony.isUnicorn = false;
		        Color color = new Color(bufferedimage.getRGB(0, 0), true);
		        Color color1 = new Color(249, 177, 49, 255);
		        Color color2 = new Color(136, 202, 240, 255);
		        Color color3 = new Color(209, 159, 228, 255);
		        Color color4 = new Color(254, 249, 252, 255);
		        if(color.equals(color1))
		        {
		        }
		        if(color.equals(color2))
		        {
		        	pony.isPegasus = true;
		        }
		        if(color.equals(color3))
		        {
		        	pony.isUnicorn = true;
		        }
		        if(color.equals(color4))
		        {
		        	pony.isPegasus = true;
		        	pony.isUnicorn = true;
		        }
		        pony.checked = pony.textureLocation;
    		
    		} catch (IOException e) {
				
			}
    	}
    	isSleeping = pony.isPlayerSleeping();
        isUnicorn = pony.isUnicorn;
        isPegasus = pony.isPegasus;
        isSneak = pony.isSneaking();
        heldItemRight = pony.getHeldItem() == null ? 0 : 1;
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        GL11.glPushMatrix();
        if(isSleeping){
        	GL11.glRotatef(90, 1, 0, 0);
        	GL11.glTranslatef(0f, -0.5f, -0.9f);
        }
    	float scale = f5;
        Head.render(scale);
        Headpiece[0].render(scale);
        Headpiece[1].render(scale);
        if(isUnicorn)
        {
            Headpiece[2].render(scale);
        }
        Helmet.render(scale);
        Body.render(scale);
        for(int i = 0; i < Bodypiece.length; i++)
        {
            Bodypiece[i].render(scale);
        }

        LeftArm.render(scale);
        RightArm.render(scale);
        LeftLeg.render(scale);
        RightLeg.render(scale);
        for(int j = 0; j < Tail.length; j++)
        {
            Tail[j].render(scale);
        }

        if(isPegasus)
        {
            if(isFlying || isSneak)
            {
                for(int k = 0; k < LeftWingExt.length; k++)
                {
                    LeftWingExt[k].render(scale);
                }

                for(int l = 0; l < RightWingExt.length; l++)
                {
                    RightWingExt[l].render(scale);
                }

            } else
            {
                for(int i1 = 0; i1 < LeftWing.length; i1++)
                {
                    LeftWing[i1].render(scale);
                }

                for(int j1 = 0; j1 < RightWing.length; j1++)
                {
                    RightWing[j1].render(scale);
                }

            }
        }
        GL11.glPopMatrix();
    }

    protected void renderGlow(RenderManager rendermanager, EntityPlayer entityplayer)
    {
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();
        if(itemstack == null)
        {
            return;
        } else
        {
            GL11.glPushMatrix();
            double d = entityplayer.posX;
            double d1 = entityplayer.posY;
            double d2 = entityplayer.posZ;
            GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
            GL11.glTranslatef((float)d + 0.0F, (float)d1 + 2.3F, (float)d2);
            GL11.glScalef(5F, 5F, 5F);
            GL11.glRotatef(-rendermanager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(rendermanager.playerViewX, 1.0F, 0.0F, 0.0F);
//            RenderEngine renderengine = rendermanager.renderEngine;
//            renderengine.bindTexture("/fx/glow.png");
            Tessellator tessellator = Tessellator.instance;
            float f = 0.0F;
            float f1 = 0.25F;
            float f2 = 0.0F;
            float f3 = 0.25F;
            float f4 = 1.0F;
            float f5 = 0.5F;
            float f6 = 0.25F;
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            tessellator.addVertexWithUV(-1D, -1D, 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV(-1D, 1.0D, 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV(1.0D, 1.0D, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV(1.0D, -1D, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
            GL11.glPopMatrix();
            return;
        }
    }
}
