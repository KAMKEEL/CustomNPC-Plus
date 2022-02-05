package noppes.npcs.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemNpcWeaponInterface extends ItemSword implements ItemRenderInterface{

    
	public ItemNpcWeaponInterface(int par1, ToolMaterial material) {
		this(material);
	}
	public ItemNpcWeaponInterface(ToolMaterial material) {
		super(material);
		setCreativeTab(CustomItems.tab);
		CustomNpcs.proxy.registerItem(this);
		setCreativeTab(CustomItems.tabWeapon);
	}
	public void renderSpecial(){
        GL11.glScalef(0.66f, 0.66f,0.66f);
        GL11.glTranslatef(0.16f, 0.26f, 0.06f);
    }

    @Override
    public Item setUnlocalizedName(String name){
		GameRegistry.registerItem(this, name);
    	return super.setUnlocalizedName(name);
    }
}
