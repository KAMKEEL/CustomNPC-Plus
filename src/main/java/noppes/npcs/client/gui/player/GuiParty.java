package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomItems;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.InventoryTabQuests;
import tconstruct.client.tabs.TabRegistry;

public class GuiParty extends GuiNPCInterface implements ITopButtonListener,ICustomScrollListener,  IGuiData {
    private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    private final EntityPlayer player;

    private boolean receivedData;
    private long renderTicks;

    public GuiParty(EntityPlayer player) {
        super();
        this.player = player;
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;

        //TODO: Send initial data packet
    }

    public void initGui(){
        super.initGui();
        guiTop +=10;

        TabRegistry.addTabsToList(buttonList);
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabQuests.class);

        GuiMenuSideButton questsButton = new GuiMenuSideButton(100, guiLeft + xSize + 37, this.guiTop + 3, 22, 22, "");
        questsButton.rightSided = true;
        questsButton.renderStack = new ItemStack(CustomItems.letter);
        addButton(questsButton);

        GuiMenuSideButton partyButton = new GuiMenuSideButton(101, guiLeft + xSize + 37, this.guiTop + 3 + 21, 22, 22, "");
        partyButton.rightSided = true;
        partyButton.active = true;
        partyButton.renderStack = new ItemStack(CustomItems.bag);
        addButton(partyButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        initGui();

        if (guibutton.id == 100) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new GuiQuestLog(mc.thePlayer));
        }
    }

    @Override
    public void drawScreen(int i, int j, float f){
        renderTicks++;

        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.drawScreen(i, j, f);

        if(!receivedData){
            String periods = "";
            for (int k = 0; k < (renderTicks/10)%4; k++) {
                periods += ".";
            }
            fontRendererObj.drawString(StatCollector.translateToLocal("gui.loading") + periods,guiLeft + xSize/2,guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
            return;
        }
    }

    @Override
    public void keyTyped(char c, int i)
    {
        if (i == 1 || i == mc.gameSettings.keyBindInventory.getKeyCode()) // inventory key
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.receivedData = true;
        initGui();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.save();
    }

    @Override
    public void save() {

    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {

    }
}
