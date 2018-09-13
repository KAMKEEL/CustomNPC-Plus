package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.controllers.RecipeController;

import org.lwjgl.opengl.GL11;

public class GuiNpcCarpentryBench extends GuiContainerNPCInterface
{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/carpentry.png");
    private ContainerCarpentryBench container;
    private GuiNpcButton button;
    
    public GuiNpcCarpentryBench(ContainerCarpentryBench container)
    {
        super(null,container);
        this.container = container;
        this.title = "";
        allowUserInput = false;//allowUserInput
        closeOnEsc = true;
        ySize = 180;
    }
    @Override
    public void initGui(){
    	super.initGui();
    	addButton(button = new GuiNpcButton(0, guiLeft + 158, guiTop + 4, 12, 20, "..."));
    }

    public void buttonEvent(GuiButton guibutton){
    	displayGuiScreen(new GuiRecipes());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
    	button.enabled = RecipeController.instance != null && !RecipeController.instance.anvilRecipes.isEmpty();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        String title = StatCollector.translateToLocal("tile.npcCarpentyBench.name");
        if(container.getMetadata() >= 4)
        	title = StatCollector.translateToLocal("tile.anvil.name");
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(f, i, j);
      	fontRendererObj.drawString(title, guiLeft + 4 , guiTop + 4 , CustomNpcResourceListener.DefaultTextColor);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), guiLeft + 4, guiTop + 87, CustomNpcResourceListener.DefaultTextColor);
    }

	@Override
	public void save() {
		return;
	}
}
