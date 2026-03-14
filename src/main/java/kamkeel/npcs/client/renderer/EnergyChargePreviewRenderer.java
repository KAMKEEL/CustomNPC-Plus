package kamkeel.npcs.client.renderer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kamkeel.npcs.controllers.data.energycharge.EnergyChargePreviewManager;
import kamkeel.npcs.entity.EntityEnergyAbility;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/**
 * Renders packet-driven charging previews through RenderManager.
 */
public class EnergyChargePreviewRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (EnergyChargePreviewManager.ClientInstance == null || !EnergyChargePreviewManager.ClientInstance.hasPreviews()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        float partialTicks = event.partialTicks;

        for (EntityEnergyAbility entity : EnergyChargePreviewManager.ClientInstance.getPreviews()) {
            if (entity == null || entity.isDead) continue;

            double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks - RenderManager.renderPosX;
            double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks - RenderManager.renderPosY;
            double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks - RenderManager.renderPosZ;

            RenderManager.instance.renderEntityWithPosYaw(entity, x, y, z, entity.rotationYaw, partialTicks);
        }
    }
}
