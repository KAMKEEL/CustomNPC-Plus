package noppes.npcs.client.guide;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/**
 * 把引路路径画成死亡空间 Locator 那样的贴地发光带：从玩家脚下发出、沿实际路线蜿蜒、
 * 能量脉冲沿线往目标跑，终点一个菱形标记。
 *
 * 1.7.10 是固定管线、用不了 shader，发光感靠加法混合叠两层实现（宽而暗的外发光 + 窄而亮
 * 的芯），流动感靠"弧长参与相位"的正弦调制，不需要任何贴图。
 */
public class GuideRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // 安全网：渲染回调里没接住的异常一样会崩游戏。这里还额外静默结束引路——渲染每帧
        // 都跑，留着只会每帧再抛一次。
        try {
            render(event.partialTicks);
        } catch (Throwable t) {
            System.out.println("[CustomNPC+] GuideRenderer 渲染时抛出未处理异常：");
            t.printStackTrace();
            GuideSession.Instance.stopSilently();
        }
    }

    private void render(float partialTicks) {
        if (mc.theWorld == null || mc.thePlayer == null || !GuideSession.Instance.isActive()) {
            return;
        }
        double[][] curve = GuideSession.Instance.getCurve();
        double[] arcs = GuideSession.Instance.getArcLengths();

        double px = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
        double py = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
        double pz = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;

        double time = mc.theWorld.getTotalWorldTime() + partialTicks;

        GL11.glPushMatrix();
        try {
            // GL 状态必须无论如何都复原，所以是 try/finally 而不只是 try/catch——一次半途
            // 抛出的异常会让后续所有渲染都花掉。
            GL11.glTranslated(-px, -py, -pz);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);   // 穿墙全亮：引路的价值就在于告诉你墙后面怎么走
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // 加法混合 = 固定管线的发光

            Tessellator tes = Tessellator.instance;

            if (curve != null && arcs != null && curve.length >= 2) {
                // 裁掉玩家已经走过的那一段，带子的头始终贴在脚下。
                int start = GuideCurve.nearestIndex(curve, px, py, pz);
                drawBand(tes, curve, arcs, start, px, py, pz, time,
                    GuideRenderConfig.GLOW_HALF_WIDTH,
                    GuideRenderConfig.GLOW_R, GuideRenderConfig.GLOW_G, GuideRenderConfig.GLOW_B,
                    GuideRenderConfig.GLOW_ALPHA_SCALE);
                drawBand(tes, curve, arcs, start, px, py, pz, time,
                    GuideRenderConfig.CORE_HALF_WIDTH,
                    GuideRenderConfig.CORE_R, GuideRenderConfig.CORE_G, GuideRenderConfig.CORE_B,
                    1.0f);
            }

            drawMarker(tes, px, py, pz, time);
        } finally {
            GL11.glLineWidth(1.0f);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPopMatrix();
        }
    }

    /**
     * 沿曲线拉一条有宽度的贴地带。用 QUAD_STRIP 而不是 LINE_STRIP：glLineWidth 在不少驱动
     * 上被钳到 1~10 像素，而且不随距离透视缩放，远处会变成一根等宽的硬线。
     *
     * 超出可见范围的顶点 alpha 会算成 0，加法混合下贡献为零，所以不需要为了跳过它们把
     * strip 断开——路径折返时断开反而会出现接缝。
     */
    private void drawBand(Tessellator tes, double[][] curve, double[] arcs, int start,
                          double px, double py, double pz, double time,
                          double halfWidth, int r, int g, int b, float alphaScale) {
        if (start < 0 || start >= curve.length - 1) {
            return;
        }
        tes.startDrawing(GL11.GL_QUAD_STRIP);
        for (int i = start; i < curve.length; i++) {
            double[] p = curve[i];

            // 前进方向：一般用前向差分（看下一个点），最后一个点没有下一个点，改用后向差分。
            double[] a = (i < curve.length - 1) ? curve[i] : curve[i - 1];
            double[] c = (i < curve.length - 1) ? curve[i + 1] : curve[i];
            double dx = c[0] - a[0];
            double dz = c[2] - a[2];
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0e-6) {
                dx = 1.0;
                dz = 0.0;
                len = 1.0;
            }
            // 水平面内垂直于前进方向的单位向量。
            double perpX = -dz / len;
            double perpZ = dx / len;

            double ddx = p[0] - px;
            double ddy = p[1] - py;
            double ddz = p[2] - pz;
            double dist = Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);

            double alpha = GuideFade.pathAlpha(dist) * GuideFade.pulse(arcs[i], time) * alphaScale;
            int a255 = toByteAlpha(alpha);

            tes.setColorRGBA(r, g, b, a255);
            tes.addVertex(p[0] - perpX * halfWidth, p[1], p[2] - perpZ * halfWidth);
            tes.addVertex(p[0] + perpX * halfWidth, p[1], p[2] + perpZ * halfWidth);
        }
        tes.draw();
    }

    /** 终点菱形：billboard 四边形，始终正对镜头，外加一圈线框，缓慢自转 + 呼吸。 */
    private void drawMarker(Tessellator tes, double px, double py, double pz, double time) {
        double[] target = GuideSession.Instance.getMarkerPos();
        if (target == null) {
            return;
        }
        double dx = target[0] - px;
        double dy = target[1] - py;
        double dz = target[2] - pz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double alpha = GuideFade.markerAlpha(dist);
        if (alpha <= 0.0) {
            return;
        }

        double breathe = 1.0 + GuideRenderConfig.MARKER_BREATHE_AMPLITUDE
                             * Math.sin(time * GuideRenderConfig.MARKER_BREATHE_SPEED);
        double size = GuideRenderConfig.MARKER_SIZE * breathe;

        GL11.glPushMatrix();
        try {
            GL11.glTranslated(target[0], target[1], target[2]);
            // 跟原版名牌一样的 billboard 变换，把四边形转到正对镜头的 XY 平面。
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0f, 0.0f, 0.0f);
            // 自转：billboard 之后绕视线轴转，标记就在屏幕平面里匀速旋转。角度先对 360 取模
            // ——time 是世界总时刻，跑久了会到千万量级，直接喂给 glRotated 精度会烂掉。
            double spin = (time * GuideRenderConfig.MARKER_SPIN_DEGREES_PER_TICK) % 360.0;
            GL11.glRotated(spin, 0.0, 0.0, 1.0);

            drawDiamond(tes, size * GuideRenderConfig.MARKER_GLOW_SCALE,
                GuideRenderConfig.GLOW_R, GuideRenderConfig.GLOW_G, GuideRenderConfig.GLOW_B,
                alpha * GuideRenderConfig.GLOW_ALPHA_SCALE);
            drawDiamond(tes, size,
                GuideRenderConfig.CORE_R, GuideRenderConfig.CORE_G, GuideRenderConfig.CORE_B,
                alpha);
            drawDiamondOutline(tes, size * GuideRenderConfig.MARKER_OUTLINE_SCALE,
                GuideRenderConfig.CORE_R, GuideRenderConfig.CORE_G, GuideRenderConfig.CORE_B,
                alpha * GuideRenderConfig.MARKER_OUTLINE_ALPHA_SCALE);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void drawDiamond(Tessellator tes, double size, int r, int g, int b, double alpha) {
        int a255 = toByteAlpha(alpha);
        tes.startDrawing(GL11.GL_TRIANGLE_FAN);
        tes.setColorRGBA(r, g, b, a255);
        tes.addVertex(0.0, 0.0, 0.0);      // 扇心
        tes.addVertex(0.0, size, 0.0);     // 上
        tes.addVertex(size, 0.0, 0.0);     // 右
        tes.addVertex(0.0, -size, 0.0);    // 下
        tes.addVertex(-size, 0.0, 0.0);    // 左
        tes.addVertex(0.0, size, 0.0);     // 回到上，闭合
        tes.draw();
    }

    /** 菱形外面那一圈线框。LINE_LOOP 自己闭合，不用重复首个顶点。 */
    private void drawDiamondOutline(Tessellator tes, double size, int r, int g, int b, double alpha) {
        int a255 = toByteAlpha(alpha);
        GL11.glLineWidth(GuideRenderConfig.MARKER_OUTLINE_WIDTH);
        tes.startDrawing(GL11.GL_LINE_LOOP);
        tes.setColorRGBA(r, g, b, a255);
        tes.addVertex(0.0, size, 0.0);
        tes.addVertex(size, 0.0, 0.0);
        tes.addVertex(0.0, -size, 0.0);
        tes.addVertex(-size, 0.0, 0.0);
        tes.draw();
        GL11.glLineWidth(1.0f);
    }

    private static int toByteAlpha(double alpha) {
        return (int) Math.max(0.0, Math.min(255.0, alpha * 255.0));
    }
}
