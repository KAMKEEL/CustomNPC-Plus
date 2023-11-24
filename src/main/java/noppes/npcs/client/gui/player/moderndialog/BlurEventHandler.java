package noppes.npcs.client.gui.player.moderndialog;

import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.io.IOException;

public class BlurEventHandler {
    @SubscribeEvent
    public void onGuiChange(GuiOpenEvent event) {
        if (Minecraft.getMinecraft().theWorld != null) {
            EntityRenderer er = Minecraft.getMinecraft().entityRenderer;
            if (er.getShaderGroup() == null && (event.gui instanceof GuiModernDialogInteract) || (event.gui instanceof GuiModernQuestDialog)) {
                loadEffect(er,new ResourceLocation("customnpcs", "shaders/post/blur.json"));
            } else if (er.getShaderGroup()!=null && !(event.gui instanceof GuiModernDialogInteract)) {
                er.deactivateShader();
            }
        }
    }
    public static void loadEffect(EntityRenderer er, ResourceLocation p_175069_1_) {
        if (er.theShaderGroup != null) {
            er.theShaderGroup.deleteShaderGroup();
        }
        Minecraft mc = Minecraft.getMinecraft();
        try {
            er.theShaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), p_175069_1_);
            er.theShaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
        } catch (IOException | JsonSyntaxException ignored) {

        }

    }
}
