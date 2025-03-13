package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.containers.ContainerAnvilRepair;
import noppes.npcs.controllers.RecipeController;
import org.lwjgl.opengl.GL11;

public class GuiNpcAnvil extends GuiContainerNPCInterface {

    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/anvil.png");
    private final ContainerAnvilRepair container;
    private GuiNpcButton button;

    public GuiNpcAnvil(ContainerAnvilRepair container) {
        super(null, container);
        this.container = container;
        this.title = "";
        allowUserInput = false;
        closeOnEsc = true;
        ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();
        addButton(button = new GuiNpcButton(0, guiLeft + 158, guiTop + 4, 12, 20, "..."));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        displayGuiScreen(new GuiAnvilRecipes());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Enable the button if there are any anvil recipes.
        button.enabled = RecipeController.Instance != null && !RecipeController.Instance.getAnvilList().isEmpty();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // Draw title and player's inventory label.
        fontRendererObj.drawString(StatCollector.translateToLocal("tile.anvil.name"), guiLeft + 4, guiTop + 4, CustomNpcResourceListener.DefaultTextColor);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), guiLeft + 4, guiTop + 87, CustomNpcResourceListener.DefaultTextColor);

        // Compute the repair status message based on container.repairCost and player's XP.
        String status = "";
        if (container.repairCost > 0) {
            if (container.repairCost > mc.thePlayer.experienceTotal) {
                status = "Repair cost: " + container.repairCost + " XP";
            } else {
                status = "Repair cost: " + container.repairCost + " XP";
            }
        } else {
            // If there is a damaged item that is fully repaired.
            if (container.anvilMatrix.getStackInRowAndColumn(0, 0) != null &&
                container.anvilMatrix.getStackInRowAndColumn(0, 0).isItemStackDamageable() &&
                container.anvilMatrix.getStackInRowAndColumn(0, 0).getItemDamage() <= 0) {
                status = "Item is already fully repaired";
            }
        }
        int textColor = CustomNpcResourceListener.DefaultTextColor;
        if (container.repairCost > mc.thePlayer.experienceTotal) {
            textColor = 0xFF0000; // red when not enough XP
        }
        fontRendererObj.drawString(status, guiLeft + 5, guiTop + 75, textColor);
    }

    @Override
    public void save() {
        // Nothing to save.
    }
}
