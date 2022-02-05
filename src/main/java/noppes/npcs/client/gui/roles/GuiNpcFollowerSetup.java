package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;


public class GuiNpcFollowerSetup extends GuiContainerNPCInterface2
{
	private RoleFollower role;
	private static final ResourceLocation field_110422_t = new ResourceLocation("textures/gui/followersetup.png");
    public GuiNpcFollowerSetup(EntityNPCInterface npc,ContainerNPCFollowerSetup container)
    {
        super(npc, container);
    	ySize = 200;
        role = (RoleFollower) npc.roleInterface;
        setBackground("followersetup.png");
    }
    public void initGui()
    {
    	super.initGui();
        for(int i = 0; i < 3; i++){
        	int x = guiLeft + 66;
        	int y = guiTop + 37;
        	y += i * 25;
        	GuiNpcTextField tf = new GuiNpcTextField(i,this, fontRendererObj,x, y, 24, 20, "1");
        	tf.numbersOnly = true;
        	tf.setMinMaxDefault(1, Integer.MAX_VALUE, 1);
        	addTextField(tf);
        }
        int i = 0;
        for(int day : role.rates.values()){
        	getTextField(i).setText(day + "");
        	i++;
        }
        addTextField(new GuiNpcTextField(3,this, fontRendererObj,guiLeft + 100, guiTop + 6, 286, 20, role.dialogHire));
        addTextField(new GuiNpcTextField(4,this, fontRendererObj,guiLeft + 100, guiTop + 30, 286, 20, role.dialogFarewell));

        addLabel(new GuiNpcLabel(7, "follower.infiniteDays", guiLeft + 180, guiTop + 80));
        addButton(new GuiNpcButtonYesNo(7, guiLeft + 260, guiTop + 75, role.infiniteDays));
        
        addLabel(new GuiNpcLabel(8, "follower.guiDisabled", guiLeft + 180, guiTop + 104));
        addButton(new GuiNpcButtonYesNo(8, guiLeft + 260, guiTop + 99, role.disableGui));
        
        addLabel(new GuiNpcLabel(9, "follower.allowSoulstone", guiLeft + 180, guiTop + 128));
        addButton(new GuiNpcButtonYesNo(9, guiLeft + 260, guiTop + 123, !role.refuseSoulStone));
        

        addButton(new GuiNpcButton(10, guiLeft + 195, guiTop + 147, 100, 20, "remote.reset"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
    	if(guibutton.id == 7){
    		role.infiniteDays = ((GuiNpcButtonYesNo)guibutton).getBoolean();
    	}
    	if(guibutton.id == 8){
    		role.disableGui = ((GuiNpcButtonYesNo)guibutton).getBoolean();
    	}
    	if(guibutton.id == 9){
    		role.refuseSoulStone = !((GuiNpcButtonYesNo)guibutton).getBoolean();
    	}
    	if(guibutton.id == 10){
    		role.killed();
    	}
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	
    }
	@Override
	public void save() {
    	HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
    	for(int i= 0;i < role.inventory.getSizeInventory();i++){
    		ItemStack item = role.inventory.getStackInSlot(i);
    		if(item != null){

    			int days = 1;
    			if(!getTextField(i).isEmpty() && getTextField(i).isInteger())
    				days = getTextField(i).getInteger();
    			if(days <= 0)
    				days = 1;
    			
    			map.put(i,days);
    		}
        }
    	role.rates = map;
    	role.dialogHire = getTextField(3).getText();
    	role.dialogFarewell = getTextField(4).getText();
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}
}
