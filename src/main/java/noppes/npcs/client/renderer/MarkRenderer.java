package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.constants.MarkType;
import noppes.npcs.controllers.data.MarkData;
import org.lwjgl.opengl.GL11;

public class MarkRenderer {
    public static ResourceLocation markExclamation = new ResourceLocation("customnpcs", "textures/marks/exclamation.png");
    public static ResourceLocation markQuestion = new ResourceLocation("customnpcs", "textures/marks/question.png");
    public static ResourceLocation markPointer = new ResourceLocation("customnpcs", "textures/marks/pointer.png");
    public static ResourceLocation markCross = new ResourceLocation("customnpcs", "textures/marks/cross.png");
    public static ResourceLocation markSkull = new ResourceLocation("customnpcs", "textures/marks/skull.png");
    public static ResourceLocation markStar = new ResourceLocation("customnpcs", "textures/marks/star.png");

    public static int displayList = -1;

    public static void render(EntityLivingBase entity, double x, double y, double z, MarkData.Mark mark) {
        GL11.glPushMatrix();
        int color = mark.color;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float green = (float) (color & 255) / 255.0F;
        GL11.glColor4f(red, blue, green, 1);
        GL11.glTranslatef((float) x, (float) (y + entity.height + 0.6), (float) z);
        GL11.glRotatef(-entity.rotationYawHead, 0, 1, 0);

        if (mark.type == MarkType.EXCLAMATION)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markExclamation);
        else if (mark.type == MarkType.QUESTION)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markQuestion);
        else if (mark.type == MarkType.POINTER)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markPointer);
        else if (mark.type == MarkType.CROSS)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markCross);
        else if (mark.type == MarkType.SKULL)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markSkull);
        else if (mark.type == MarkType.STAR)
            Minecraft.getMinecraft().getTextureManager().bindTexture(markStar);

        if (displayList >= 0) {
            GL11.glCallList(displayList);
        } else {
            displayList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            GL11.glTranslatef((float) -0.5, 0, 0);
            Model2DRenderer.renderItemIn2D(0f, 0f, 1f, 1f, 32, 32, 0.0625F);
            GL11.glEndList();
        }

        GL11.glPopMatrix();
    }
}
