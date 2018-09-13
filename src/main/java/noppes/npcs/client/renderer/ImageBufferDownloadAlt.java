package noppes.npcs.client.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import net.minecraft.client.renderer.ImageBufferDownload;

public class ImageBufferDownloadAlt extends ImageBufferDownload
{
    private int imageData[];
    private int imageWidth;
    private int imageHeight;

    @Override
    public BufferedImage parseUserSkin(BufferedImage bufferedimage)
    {        
		imageWidth = bufferedimage.getWidth(null);
        imageHeight = imageWidth / 2;
        
        BufferedImage bufferedimage1 = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics g = bufferedimage1.getGraphics();
        g.drawImage(bufferedimage, 0, 0, null);
        g.dispose();
        imageData = ((DataBufferInt)bufferedimage1.getRaster().getDataBuffer()).getData();
        setAreaTransparent(imageWidth / 2, 0, imageWidth, imageHeight / 2);
        return bufferedimage1;
    }
    /**
     * Makes the given area of the image transparent if it was previously completely opaque (used to remove the outer
     * layer of a skin around the head if it was saved all opaque; this would be redundant so it's assumed that the skin
     * maker is just using an image editor without an alpha channel)
     */
    private void setAreaTransparent(int par1, int par2, int par3, int par4)
    {
        if (!this.hasTransparency(par1, par2, par3, par4))
        {
            for (int i1 = par1; i1 < par3; ++i1)
            {
                for (int j1 = par2; j1 < par4; ++j1)
                {
                    this.imageData[i1 + j1 * this.imageWidth] &= 16777215;
                }
            }
        }
    }

    /**
     * Returns true if the given area of the image contains transparent pixels
     */
    private boolean hasTransparency(int par1, int par2, int par3, int par4)
    {
        for (int i1 = par1; i1 < par3; ++i1)
        {
            for (int j1 = par2; j1 < par4; ++j1)
            {
                int k1 = this.imageData[i1 + j1 * this.imageWidth];

                if ((k1 >> 24 & 255) < 128)
                {
                    return true;
                }
            }
        }

        return false;
    }
//        
//	 private boolean loadPlayerData(BufferedImage bufferedimage,EntityPlayer player) {
//		if(player == null)
//			return false;
//		PlayerData data = RenderMorePlayer.getPlayerData(player);
//		data.setPlayerModel(checkSkin(bufferedimage,player.username));
//		data.isChanged = true;
//		return data.model != null;
//	}
//	public static ModelData checkSkin(BufferedImage bufferedimage,String player){
//		if(bufferedimage == null)
//			return null;
//		
//		if (!new Color(bufferedimage.getRGB(0, 0), true).equals(new Color(128, 128, 128, 255))) {
//			return null;
//		}
//		ModelData data = new ModelData();
//		Color type = new Color(bufferedimage.getRGB(1, 0), true);
//		if (type.equals(new Color(255, 0, 255, 255))) {
//			data.type = EnumPlayerModelType.HUMANFEMALE;
//			data.mainModel = new ModelHumanFemale(data,0);
//			data.modelArmor = new ModelHumanFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelHumanFemale(data,0.8f);
//			data.scaleX = 0.9075f;
//			data.scaleY = 0.9075f;
//			data.scaleZ = 0.9075f;
//		} else if (type.equals(new Color(128, 0, 0, 255))) {
//			data.type = EnumPlayerModelType.DWARFMALE;
//			data.mainModel = new ModelDwarfMale(data,0);
//			data.modelArmor = new ModelDwarfMale(data,0.3f);
//			data.modelArmorChestplate = new ModelDwarfMale(data,0.8f);
//			data.scaleX = 0.85f;
//			data.scaleY = 0.6875f;
//			data.scaleZ = 0.85f;
//		} else if (type.equals(new Color(255, 0, 0, 255))) {
//			data.type = EnumPlayerModelType.DWARFFEMALE;
//			data.mainModel = new ModelDwarfFemale(data,0);
//			data.modelArmor = new ModelDwarfFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelDwarfFemale(data,0.8f);
//			data.scaleX = 0.75f;
//			data.scaleY = 0.6275f;
//			data.scaleZ = 0.75f;
//		} else if (type.equals(new Color(0, 128, 0, 255))) {
//			data.type = EnumPlayerModelType.ORCMALE;
//			data.mainModel = new ModelOrcMale(data,0);
//			data.modelArmor = new ModelOrcMale(data,0.3f);
//			data.modelArmorChestplate = new ModelOrcMale(data,0.8f);
//			data.scaleX = 1.2f;
//			data.scaleY = 1f;
//			data.scaleZ = 1.2f;
//		} else if (type.equals(new Color(0, 255, 0, 255))) {
//			data.type = EnumPlayerModelType.ORCFEMALE;
//			data.mainModel = new ModelOrcFemale(data,0);
//			data.modelArmor = new ModelOrcFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelOrcFemale(data,0.8f);
//		} else if (type.equals(new Color(0, 0, 128, 255))) {
//			data.type = EnumPlayerModelType.FURRYMALE;
//			data.mainModel = new ModelFurryMale(data,0);
//			data.modelArmor = new ModelFurryMale(data,0.3f);
//			data.modelArmorChestplate = new ModelFurryMale(data,0.8f);
//		} else if (type.equals(new Color(0, 0, 255, 255))) {
//			data.type = EnumPlayerModelType.FURRYFEMALE;
//			data.mainModel = new ModelFurryFemale(data,0);
//			data.modelArmor = new ModelFurryFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelFurryFemale(data,0.8f);
//			data.scaleX = 0.9075f;
//			data.scaleY = 0.9075f;
//			data.scaleZ = 0.9075f;
//		}else if (type.equals(new Color(255, 128, 0, 255))) {
//			data.type = EnumPlayerModelType.MONSTERMALE;
//			data.mainModel = new ModelMonsterMale(data,0);
//			data.modelArmor = new ModelMonsterMale(data,0.3f);
//			data.modelArmorChestplate = new ModelMonsterMale(data,0.8f);
//		} else if (type.equals(new Color(255, 255, 0, 255))) {
//			data.type = EnumPlayerModelType.MONSTERFEMALE;
//			data.mainModel = new ModelMonsterFemale(data,0);
//			data.modelArmor = new ModelMonsterFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelMonsterFemale(data,0.8f);
//			data.scaleX = 0.9075f;
//			data.scaleY = 0.9075f;
//			data.scaleZ = 0.9075f;
//		}else if (type.equals(new Color(0,255, 128, 255))) {
//			data.type = EnumPlayerModelType.ELFMALE;
//			data.mainModel = new ModelElfMale(data,0);
//			data.modelArmor = new ModelElfMale(data,0.3f);
//			data.modelArmorChestplate = new ModelElfMale(data,0.8f);
//			data.scaleX = 0.85f;
//			data.scaleY = 1.07f;
//			data.scaleZ = 0.85f;
//		} else if (type.equals(new Color(0, 255, 255, 255))) {
//			data.type = EnumPlayerModelType.ELFFEMALE;
//			data.mainModel = new ModelElfFemale(data,0);
//			data.modelArmor = new ModelElfFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelElfFemale(data,0.8f);
//			data.scaleX = 0.8f;
//			data.scaleY = 1f;
//			data.scaleZ = 0.8f;
//		} else if (type.equals(new Color(32, 32, 32, 255))) {
//			data.type = EnumPlayerModelType.ENDER;
//			data.mainModel = new ModelEnderChibi(data,0);
//			data.modelArmor = new ModelEnderChibi(data,0.3f);
//			data.modelArmorChestplate = new ModelEnderChibi(data,0.8f);
//		} 
//		else if (type.equals(new Color(128, 255, 0, 255))) {
//			data.type = EnumPlayerModelType.NAGAMALE;
//			data.mainModel = new ModelNagaMale(data,0);
//			data.modelArmor = new ModelNagaMale(data,0.3f);
//			data.modelArmorChestplate = new ModelNagaMale(data,0.8f);
//		} 
//		else if (type.equals(new Color(128, 128, 0, 255))) {
//			data.type = EnumPlayerModelType.NAGAFEMALE;
//			data.mainModel = new ModelNagaFemale(data,0);
//			data.modelArmor = new ModelNagaFemale(data,0.3f);
//			data.modelArmorChestplate = new ModelNagaFemale(data,0.8f);
//		} 
//		else if(RenderMorePlayer.superUsers.contains(player)){
//			if (type.equals(new Color(66, 66, 66, 255))){
//				data.mainModel = new ModelCrystal(data,0);
//				data.modelArmor = new ModelCrystal(data,0.3f);
//				data.modelArmorChestplate = new ModelCrystal(data,0.8f);
//			}
//			else if (type.equals(new Color(13, 13, 13, 255))){
//				data.mainModel = new ModelFail(data);
//			}
//		}
//		
//		Color sizeColor = new Color(bufferedimage.getRGB(2, 0), true);
//		int size = 3;
//		if (sizeColor.equals(new Color(255, 0, 0, 255)))
//			size = 4;
//		else if (sizeColor.equals(new Color(0, 255, 0, 255)))
//			size = 2;
//		else if (sizeColor.equals(new Color(0, 0, 255, 255)))
//			size = 1;
//		else if (sizeColor.equals(new Color(255, 0, 255, 255)))
//			size = 0;
//		data.size = size;
//		
//		Color headVissible = new Color(bufferedimage.getRGB(3, 0), true);
//		if (headVissible.equals(new Color(255, 0, 0, 255)))
//			data.showHelmet = false;
//		else if (headVissible.equals(new Color(0, 255, 0, 255)))
//			data.showHeadwear = false;
//		else if (headVissible.equals(new Color(255, 255, 0, 255))){
//			data.showHeadwear = false;
//			data.showHelmet = false;
//		}
//		
//		
//		int extraOption1 = getValue(new Color(bufferedimage.getRGB(7, 0), true));
//		data.mainModel.setExtraOption1(extraOption1);
//		data.modelArmorChestplate.setExtraOption1(extraOption1);
//		data.modelArmor.setExtraOption1(extraOption1);
//		
//		int extraOption2 = getValue(new Color(bufferedimage.getRGB(7, 1), true));
//		data.mainModel.setExtraOption2(extraOption2);
//		data.modelArmorChestplate.setExtraOption2(extraOption2);
//		data.modelArmor.setExtraOption2(extraOption2);
//		
//		int extraOption3 = getValue(new Color(bufferedimage.getRGB(7, 2), true));
//		data.mainModel.setExtraOption3(extraOption3);
//		data.modelArmorChestplate.setExtraOption3(extraOption3);
//		data.modelArmor.setExtraOption3(extraOption3);
//
//		Color color = new Color(bufferedimage.getRGB(7, 3), true);
//		data.mainModel.setExtraColor(color);
//
//		Color claws = new Color(bufferedimage.getRGB(0, 1), true);
//		if (claws.equals(new Color(255, 0, 0, 255)))
//			((ModelInterface)data.mainModel).addClaws();
//
//		data.headSize = getValue(new Color(bufferedimage.getRGB(1, 1), true));
//		
//		data.bellySize = getValue(new Color(bufferedimage.getRGB(2, 1), true));
//		return data;
//	}
//	private static int getValue(Color color){
//		if (color.equals(new Color(255, 0, 0, 255)))
//			return 1;
//		else if (color.equals(new Color(0, 255, 0, 255)))
//			return 2;
//		else if (color.equals(new Color(0, 0, 255, 255)))
//			return 3;
//		else if (color.equals(new Color(255, 0, 255, 255)))
//			return 4;
//		return 0;
//	}
}
