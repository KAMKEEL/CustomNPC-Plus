package noppes.npcs.client.gui.player;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ITopButtonListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class GuiTransportSelection extends GuiNPCInterface implements ITopButtonListener,IScrollData{

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/smallbg.png");
    protected int xSize;
    protected int guiLeft;
    protected int guiTop;
    
    private GuiCustomScroll scroll;
    
	public GuiTransportSelection(EntityNPCInterface npc) {
		super(npc);
        xSize = 176;
        this.drawDefaultBackground = false;
        title = "";
	}
    public void initGui()
    {
        super.initGui();
        guiLeft = (width - xSize) / 2;
        guiTop = (height - 222) / 2;
        //String name = "Location: " + npc.getDataWatcher().getWatchableObjectString(11);
        String name = "";
        addLabel(new GuiNpcLabel(0,name, guiLeft + (xSize - this.fontRendererObj.getStringWidth(name))/2, guiTop + 10));
        addButton(new GuiNpcButton(0, guiLeft+ 10, guiTop + 192,156,20, StatCollector.translateToLocal("transporter.travel")));
        if(scroll == null)
        	scroll = new GuiCustomScroll(this,0);
        scroll.setSize(156, 165);
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = guiTop + 20;
        addScroll(scroll);
    }
    
    @Override
    public void drawScreen(int i, int j, float f)
    {
    	drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 222);
        super.drawScreen(i, j, f);
    }

    @Override
	protected void actionPerformed(GuiButton guibutton)
    {
    	GuiNpcButton button = (GuiNpcButton) guibutton;
    	String sel = scroll.getSelected();
    	if(button.id == 0 && sel != null){
            close();
        	NoppesUtilPlayer.sendData(EnumPlayerPacket.Transport,sel);
    	}
    }
//    @Override
//    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
//    {
//    }
    @Override
    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    	scroll.mouseClicked(i, j, k);
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
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		scroll.setList(list);
	}
	@Override
	public void setSelected(String selected) {
		// TODO Auto-generated method stub
		
	}

}
