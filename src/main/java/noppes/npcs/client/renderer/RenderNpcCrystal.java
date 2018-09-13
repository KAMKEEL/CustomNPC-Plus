package noppes.npcs.client.renderer;

import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.entity.EntityNpcCrystal;

public class RenderNpcCrystal extends RenderNPCInterface
{
	ModelNpcCrystal mainmodel;
    public RenderNpcCrystal(ModelNpcCrystal model)
    {
    	super(model,0);
    	mainmodel = model;
    }
}
