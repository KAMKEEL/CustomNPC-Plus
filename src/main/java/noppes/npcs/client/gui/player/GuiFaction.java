package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.Faction;
import noppes.npcs.controllers.PlayerFactionData;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.InventoryTabFactions;
import tconstruct.client.tabs.TabRegistry;

import java.util.ArrayList;

public class GuiFaction extends GuiNPCInterface implements IGuiData{

	private int xSize;
	private int ySize;
    private int guiLeft;
    private int guiTop;
    
    private ArrayList<Faction> playerFactions = new ArrayList<Faction>();

	private int page = 0;
	private int pages = 1;

	private GuiButtonNextPage buttonNextPage;
	private GuiButtonNextPage buttonPreviousPage;
	private ResourceLocation indicator;

	public GuiFaction() {
		super();
        xSize = 200;
        ySize = 195;
        this.drawDefaultBackground = false;
        title = "";
        NoppesUtilPlayer.sendData(EnumPlayerPacket.FactionsGet);
        indicator = getResource("standardbg.png");
	}

	@Override
    public void initGui()
    {
        super.initGui();
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2 + 12;


		TabRegistry.updateTabValues(guiLeft, guiTop + 8, InventoryTabFactions.class);
		TabRegistry.addTabsToList(buttonList);
        
        this.buttonList.add(buttonNextPage = new GuiButtonNextPage(1, guiLeft + xSize - 43, guiTop + 180, true));
        this.buttonList.add(buttonPreviousPage = new GuiButtonNextPage(2, guiLeft + 20, guiTop + 180, false));
        updateButtons();
    }

	@Override
    public void drawScreen(int i, int j, float f)
    {
    	drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(indicator);
        drawTexturedModalRect(guiLeft, guiTop + 8, 0, 0, xSize, ySize);
        drawTexturedModalRect(guiLeft + 4, guiTop + 8, 56, 0, 200, ySize);
        
        if(playerFactions.isEmpty()){
        	String noFaction = StatCollector.translateToLocal("faction.nostanding");
            fontRendererObj.drawString(noFaction,guiLeft + (xSize - fontRendererObj.getStringWidth(noFaction)) / 2,guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
        }
        else
        	renderScreen();
        
        super.drawScreen(i, j, f);
        
    }

	private void renderScreen(){
        int size = 5;
        if(playerFactions.size() % 5 != 0 && page == pages)
        	size = playerFactions.size() % 5;
        
        for(int id = 0 ; id < size; id++){
        	drawHorizontalLine(guiLeft + 2, guiLeft + xSize, guiTop + 14 + id * 30, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        	
        	Faction faction = playerFactions.get((page - 1) * 5 + id);
        	String name = faction.name;
        	String points = " : " + faction.defaultPoints;
            
            String standing = StatCollector.translateToLocal("faction.friendly");
            int color = 0x00FF00;
            if(faction.defaultPoints < faction.neutralPoints){
            	standing = StatCollector.translateToLocal("faction.unfriendly");
            	color = 0xFF0000;
            	points += "/" + faction.neutralPoints;
            }
            else if(faction.defaultPoints < faction.friendlyPoints){
            	standing = StatCollector.translateToLocal("faction.neutral");
            	color = 0xF2FF00;
            	points += "/" + faction.friendlyPoints;
            }
            else{
            	points += "/-";
            }

            fontRendererObj.drawString(name, guiLeft + (xSize - fontRendererObj.getStringWidth(name)) / 2, guiTop + 19 + id * 30, faction.color);

            fontRendererObj.drawString(standing, width / 2 - fontRendererObj.getStringWidth(standing) - 1, guiTop + 33 + id * 30, color);
            fontRendererObj.drawString(points, width / 2, guiTop + 33 + id * 30, CustomNpcResourceListener.DefaultTextColor);
        }    
    	drawHorizontalLine(guiLeft + 2, guiLeft + xSize, guiTop + 14 + size * 30, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor); 
        
        if(pages > 1){
        	String s = page +"/" + pages;
        	fontRendererObj.drawString(s, guiLeft + (xSize - fontRendererObj.getStringWidth(s)) / 2, guiTop + 203, CustomNpcResourceListener.DefaultTextColor);
        }
    }
    
    @Override
	protected void actionPerformed(GuiButton guibutton){
    	if(!(guibutton instanceof GuiButtonNextPage))
    		return;
		int id = guibutton.id;
		if(id == 1){
			page++;
		}
		if(id == 2){
			page--;
		}
		updateButtons();
    }
    private void updateButtons(){
		buttonNextPage.setVisible(page < pages);
		buttonPreviousPage.setVisible(page > 1);
    }
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
    	
    }
    
    @Override
    public void keyTyped(char c, int i)
    {
        if (i == 1 || isInventoryKey(i))
        {
            close();
        }
    }
	@Override
	public void save() {
	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		playerFactions = new ArrayList<Faction>();

		NBTTagList list = compound.getTagList("FactionList", 10);
		for(int i = 0; i < list.tagCount(); i++){
			Faction faction = new Faction();
			faction.readNBT(list.getCompoundTagAt(i));
			playerFactions.add(faction);
		}
		PlayerFactionData data = new PlayerFactionData();
		data.loadNBTData(compound);
		for(int id : data.factionData.keySet()){
			int points = data.factionData.get(id);
			for(Faction faction : playerFactions){
				if(faction.id == id)
					faction.defaultPoints = points;
			}
		}
		
		pages = (playerFactions.size() - 1) / 5 ;
		pages++;
		
		page = 1;

		updateButtons();
	}

}