package noppes.npcs.items;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSkirt extends ItemArmor{

	private String texture;
	
	public ItemSkirt(ArmorMaterial par2EnumArmorMaterial, String texture) {
		super(par2EnumArmorMaterial, 0, 2);
		this.texture = texture;
		setCreativeTab(CustomItems.tabArmor);
		setMaxStackSize(1);
	}
    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
        int j = this.getColor(par1ItemStack);

        if (j < 0)
        {
            j = 16777215;
        }

        return j;
    }
	
	@Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type){
		if(type != null && type.equals("overlay"))
			return null;
		return texture;
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot){
        return CustomNpcs.proxy.getSkirtModel();
    }

    @Override
    public Item setUnlocalizedName(String name){
		GameRegistry.registerItem(this, name);
    	return super.setUnlocalizedName(name);
    }
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int par1, int par2)
    {
		return super.getIconFromDamage(par1);
    }
}
