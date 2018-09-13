package noppes.npcs.enchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.items.ItemStaff;

public abstract class EnchantInterface extends Enchantment{

    private static EnumEnchantmentType CustomNpcsType;

	public static EnchantInterface Damage;
	public static EnchantInterface Poison;
	public static EnchantInterface Confusion;
	public static EnchantInterface Infinite;
	
	private Class[] classes;

	protected EnchantInterface(int par2, Class... obs) {
		super(CustomNpcs.EnchantStartId++, par2, CustomNpcsType);
		classes = obs;
	}
	
	@Override
    public boolean canApply(ItemStack par1ItemStack){
        if(par1ItemStack.getItem() == null)
        	return false;
        for(Class cls : classes){
        	if(cls.isInstance(par1ItemStack.getItem()))
        		return true;
        }
    	return false;
    }
	
	public static void load(){
		if(!CustomNpcs.DisableEnchants){
			CustomNpcsType = EnumHelper.addEnchantmentType("customnpcs_enchants");
			try{
		        Damage = new EnchantDamage();
		        Poison = new EnchantPoison();
		        Confusion = new EnchantConfusion();
		        Infinite = new EnchantInfinite();
			}
			catch(Exception e){
				LogWriter.except(e);
			}
		}
	}

	public static int getLevel(EnchantInterface enchant, ItemStack stack) {
		if(CustomNpcs.DisableEnchants || enchant == null)
			return 0;
		return EnchantmentHelper.getEnchantmentLevel(enchant.effectId, stack);
	}
}
