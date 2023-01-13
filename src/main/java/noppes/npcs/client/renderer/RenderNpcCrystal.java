package noppes.npcs.client.renderer;

import noppes.npcs.client.model.ModelNpcCrystal;

public class RenderNpcCrystal extends RenderNPCInterface
{
	ModelNpcCrystal mainmodel;
    public RenderNpcCrystal(ModelNpcCrystal model)
    {
    	super(model,0);
    	mainmodel = model;
    }
}
