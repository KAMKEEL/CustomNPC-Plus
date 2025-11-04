package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.controllers.data.BossBarData;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;

/**
 * 自定义NPC血条渲染器
 */
public class CustomBossStatus extends Gui {
    // 自定义材质路径
    private static final ResourceLocation DEFAULT_CUSTOM_BAR_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/bossbar/boss_bar.png");
    private static final ResourceLocation DEFAULT_CUSTOM_BAR_BACKGROUND_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/bossbar/boss_bar_background.png");

    private static EntityNPCInterface statusBarEntity;
    private static int statusBarTime;

    // 纹理信息缓存
    private static class TextureInfo {
        int width;
        int height;

        TextureInfo(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }


    /**
     * 设置要显示血条的NPC
     */
    public static void setBossStatus(EntityNPCInterface npc, boolean show) {
        statusBarEntity = npc;
        statusBarTime = 100;
    }

    /**
     * 渲染自定义血条
     */
    public static void renderBossHealth() {
        if (statusBarEntity != null && statusBarTime > 0) {
            --statusBarTime;

            Minecraft minecraft = Minecraft.getMinecraft();
            FontRenderer fontRenderer = minecraft.fontRenderer;
            ScaledResolution scaledResolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);

            BossBarData bossBarData = statusBarEntity.display.bossBarData;

            // 检查NPC是否启用了自定义血条
            boolean useCustomBossBar = bossBarData.isBossBarEnabled();

            // 如果NPC未启用自定义血条，不渲染任何内容（让原版血条处理）
            if (!useCustomBossBar) {
                return;
            }

            // 清除原版血条，因为我们要使用自定义渲染
            BossStatus.statusBarTime = 0;

            // 使用自定义血条渲染
            // 获取纹理尺寸信息
            TextureInfo bgTextureInfo = getTextureInfo(getBackgroundTexture(bossBarData));
            TextureInfo barTextureInfo = getTextureInfo(getBarTexture(bossBarData));

            // 使用背景纹理的尺寸作为基准，如果没有背景纹理则使用血条纹理尺寸
            int baseWidth = bgTextureInfo.width > 0 ? bgTextureInfo.width : (barTextureInfo.width > 0 ? barTextureInfo.width : 182);
            int baseHeight = bgTextureInfo.height > 0 ? bgTextureInfo.height : (barTextureInfo.height > 0 ? barTextureInfo.height : 5);

            // 应用缩放
            float scale = bossBarData.getBossBarScale();
            int scaledBarWidth = (int)(baseWidth * scale);
            int scaledBarHeight = (int)(baseHeight * scale);

            // 计算位置（包含偏移）
            int x = scaledResolution.getScaledWidth() / 2 - scaledBarWidth / 2 + bossBarData.getBossBarOffsetX();
            int y = 12 + bossBarData.getBossBarOffsetY();

            // 启用混合和透明度
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // 渲染血条背景
            ResourceLocation backgroundTexture = getBackgroundTexture(bossBarData);
            if (backgroundTexture != null) {
                minecraft.getTextureManager().bindTexture(backgroundTexture);

                // 应用背景颜色
                int bgColor = bossBarData.getBossBarBackgroundColor();
                float bgRed = ((bgColor >> 16) & 0xFF) / 255.0f;
                float bgGreen = ((bgColor >> 8) & 0xFF) / 255.0f;
                float bgBlue = (bgColor & 0xFF) / 255.0f;
                GL11.glColor4f(bgRed, bgGreen, bgBlue, 1.0f);

                drawTexturedRect(x, y, 0.0f, 0.0f, scaledBarWidth, scaledBarHeight);
            }

            // 计算血条长度比例
            float currentHealth = statusBarEntity.getHealth();
            float maxHealth = statusBarEntity.getMaxHealth();

            // 防止除零错误
            if (maxHealth <= 0) {
                maxHealth = 1.0f;
            }

            float healthPercentage = currentHealth / maxHealth;
            // 确保百分比在0-1之间
            healthPercentage = Math.max(0.0f, Math.min(1.0f, healthPercentage));

            int healthWidth = (int)(scaledBarWidth * healthPercentage);

            // 渲染血条 - 即使healthWidth为0也要渲染（满血时应该显示满条）
            if (healthPercentage > 0) {
                ResourceLocation barTexture = getBarTexture(bossBarData);
                if (barTexture != null) {
                    minecraft.getTextureManager().bindTexture(barTexture);

                    // 应用血条颜色
                    int barColor = bossBarData.getBossBarColor();
                    float barRed = ((barColor >> 16) & 0xFF) / 255.0f;
                    float barGreen = ((barColor >> 8) & 0xFF) / 255.0f;
                    float barBlue = (barColor & 0xFF) / 255.0f;
                    GL11.glColor4f(barRed, barGreen, barBlue, 1.0f);

                    drawTexturedRect(x, y, 0.0f, 0.0f, healthWidth, scaledBarHeight);
                }
            }

            // 渲染NPC名称
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            String bossName = statusBarEntity.getCommandSenderName();
            int nameColor = statusBarEntity.faction != null ? statusBarEntity.faction.color : 0xFFFFFF;
            int nameX = x + scaledBarWidth / 2 - fontRenderer.getStringWidth(bossName) / 2;
            int nameY = y - 10;
            fontRenderer.drawStringWithShadow(bossName, nameX, nameY, nameColor);

            // 重置颜色和混合状态
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     * 获取血条背景纹理
     */
    private static ResourceLocation getBackgroundTexture(BossBarData bossBarData) {
        String texturePath = bossBarData.getBossBarBackgroundTexture();
        if (texturePath != null && !texturePath.isEmpty()) {
            try {
                return new ResourceLocation(texturePath);
            } catch (Exception e) {
                // 如果自定义纹理无效，使用默认纹理
                return DEFAULT_CUSTOM_BAR_BACKGROUND_TEXTURE;
            }
        }
        return DEFAULT_CUSTOM_BAR_BACKGROUND_TEXTURE;
    }

    /**
     * 获取血条纹理
     */
    private static ResourceLocation getBarTexture(BossBarData bossBarData) {
        String texturePath = bossBarData.getBossBarTexture();
        if (texturePath != null && !texturePath.isEmpty()) {
            try {
                return new ResourceLocation(texturePath);
            } catch (Exception e) {
                // 如果自定义纹理无效，使用默认纹理
                return DEFAULT_CUSTOM_BAR_TEXTURE;
            }
        }
        return DEFAULT_CUSTOM_BAR_TEXTURE;
    }

    /**
     * 使用Tessellator绘制纹理矩形，支持任意尺寸的纹理
     * 使用标准化的UV坐标 (0.0-1.0)
     */
    private static void drawTexturedRect(int x, int y, float u, float v, int width, int height) {
        // 假设我们要绘制整个纹理，UV坐标从0到1
        float u1 = u;
        float v1 = v;
        float u2 = u1 + 1.0f;
        float v2 = v1 + 1.0f;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, u1, v2);
        tessellator.addVertexWithUV(x + width, y + height, 0, u2, v2);
        tessellator.addVertexWithUV(x + width, y, 0, u2, v1);
        tessellator.addVertexWithUV(x, y, 0, u1, v1);
        tessellator.draw();
    }

    /**
     * 获取纹理尺寸信息
     */
    private static TextureInfo getTextureInfo(ResourceLocation texture) {
        if (texture == null) {
            return new TextureInfo(0, 0);
        }

        try {
            // 尝试从资源包中读取纹理信息
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream();
            BufferedImage image = ImageIO.read(inputStream);
            inputStream.close();

            if (image != null) {
                return new TextureInfo(image.getWidth(), image.getHeight());
            }
        } catch (Exception e) {
            // 如果无法读取纹理，使用默认尺寸
        }

        // 默认尺寸
        return new TextureInfo(182, 5);
    }
}
